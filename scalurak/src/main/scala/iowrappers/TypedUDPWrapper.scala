package scalurak.iowrappers

import scala.language.implicitConversions
import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Props, actorRef2Scala}
import akka.io.{IO, Udp}

object TypedUDPWrapper {
    type Event = Udp.Event
    // This actor accepts UDP commands from an external actor as well as UDP events from its underlying UDP socket
    type Command = Udp.Event | Udp.Command
    def props(handler: ActorRef, address: InetSocketAddress): Props = Props(new TypedUDPWrapper(handler, address))
}

class TypedUDPWrapper(handler: ActorRef, address: InetSocketAddress) extends Actor {
    import context.system
    import TypedUDPWrapper._

    IO(Udp) ! Udp.Bind(self, address)

    def receive = {
        case b: Udp.Bound =>
            context.become(ready(sender()))
            handler ! b
        case f: Udp.CommandFailed =>
            handler ! f
            context stop self
    }

    def ready(socket: ActorRef): Receive = {
        // From UDP socket
        case r: Udp.Received => handler ! r
        case Udp.Unbound =>
            context.stop(self)
            handler ! Udp.Unbound
        case f: Udp.CommandFailed =>
            handler ! f
            context stop self
        // From external actor
        case s: Udp.Send => socket ! s
        case Udp.Unbind => socket ! Udp.Unbind
    }
}
