package scalurak.iowrappers

import scala.language.implicitConversions
import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Props, actorRef2Scala}
import akka.io.{IO, Tcp}

object TypedTCPWrapper {
    sealed trait Event
    final case class Bound(tcpEvent: Tcp.Bound, sender: ActorRef) extends Event
    final case class CommandFailed(tcpEvent: Tcp.CommandFailed, sender: ActorRef) extends Event
    final case class Connected(tcpEvent: Tcp.Connected, sender: ActorRef) extends Event
    def props(handler: ActorRef, address: InetSocketAddress): Props = Props(new TypedTCPWrapper(handler, address))
}

class TypedTCPWrapper(handler: ActorRef, address: InetSocketAddress) extends Actor {
    import context.system
    import TypedTCPWrapper._

    IO(Tcp) ! Tcp.Bind(self, address)

    def receive = {
      case b: Tcp.Bound => handler ! Bound(b, sender())

      case f: Tcp.CommandFailed =>
        handler ! CommandFailed(f, sender())
        context stop self

      case c: Tcp.Connected => handler ! Connected(c, sender())
    }
}
