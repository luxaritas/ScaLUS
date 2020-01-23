package scalurak

import akka.actor.typed.{Behavior, ActorRef}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, AbstractBehavior}
import akka.io.{Tcp, Udp}
import akka.util.ByteString

object Reliability extends Enumeration {
  type Reliability = Value

  val Unreliable = Value("u")
  val UnreliableSequenced = Value("us")
  val Reliable = Value("r")
  val ReliableOrdered = Value("ro")
  val ReliableSequenced = Value("rs")
}

object Connection {
    final case class Register(handler: ActorRef[Connection.Event])
    final case class Send(data: ByteString, reliability: Reliability.Reliability)
    type Command = Register | Send | Tcp.Event | Udp.Event

    type Event = UserPacket
    final case class UserPacket(data: ByteString)

    def apply(tcpConnection: ActorRef[Tcp.Command]): Behavior[Command] = Behaviors.setup(context => new Connection(context, tcpConnection))
}

class Connection(context: ActorContext[Connection.Command], tcpConnection: ActorRef[Tcp.Command]) extends AbstractBehavior[Connection.Command](context) {
    import Connection._
    var handler: ActorRef[Connection.Event] = null

    def onMessage(msg: Command): Behavior[Command] = {
        msg match {
            case Register(handler) => this.handler = handler
            case Send(data, reliability) => ()
            case Tcp.Received(data) => ()
            case Udp.Received(data) => ()
            case Tcp.PeerClosed => ()
            // We shouldn't need to handle other events, eg those for the server and not the connection
            case _: Tcp.Event => ()
            case _: Udp.Event => ()
        }
        this
    }
}
