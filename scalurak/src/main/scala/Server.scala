package scalurak

import scala.language.implicitConversions
import scala.collection.mutable
import java.net.InetSocketAddress
import akka.actor.actorRef2Scala
import akka.actor.typed.{Behavior, ActorRef}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, AbstractBehavior}
import akka.actor.typed.scaladsl.adapter._
import akka.io.{IO, Tcp, Udp}
import scalurak.iowrappers.{TypedTCPWrapper, TypedUDPWrapper}

object Server {
    type Command = TypedTCPWrapper.Event | Udp.Event

    final case class Connected(remoteAddress: InetSocketAddress, localAddress: InetSocketAddress, connection: ActorRef[Connection.Command])
    case object CommandFailed
    type Event = Connected | CommandFailed.type

    def apply(handler: ActorRef[Event], address: InetSocketAddress): Behavior[Command] = Behaviors.setup(context => new Server(context, handler, address))
}

class Server(context: ActorContext[Server.Command], handler: ActorRef[Server.Event], address: InetSocketAddress) extends AbstractBehavior[Server.Command](context) {
    import Server._

    val connections = mutable.Map[InetSocketAddress, ActorRef[Connection.Command]]()

    val tcp = context.actorOf(TypedTCPWrapper.props(context.self.toClassic, address), "TCPWrapper")
    val udp = context.actorOf(TypedUDPWrapper.props(context.self.toClassic, address), "UDPWrapper")

    def onMessage(msg: Command): Behavior[Command] = {
        msg match {
            case TypedTCPWrapper.CommandFailed(_, _) => return Behaviors.stopped
            case TypedTCPWrapper.Connected(tcpEvent, sender) =>
                val conn = context.spawn(Connection(sender), s"scalurakconn-${tcpEvent.remoteAddress.toString}")
                connections(tcpEvent.remoteAddress) = conn
                sender ! Tcp.Register(conn.toClassic)
                handler ! Connected(tcpEvent.remoteAddress, tcpEvent.localAddress, conn)
            case r @ Udp.Received(_, remote) =>
                if (connections.isDefinedAt(remote)) connections(remote) ! r
            case Udp.CommandFailed => return Behaviors.stopped

            // We shouldn't need to do anything with these
            case TypedTCPWrapper.Bound(_, _) => ()
            case _: Udp.Event => ()
        }
        this
    }
}
