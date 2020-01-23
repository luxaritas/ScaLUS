package scalurak.example

import java.net.InetSocketAddress
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, AbstractBehavior}
import scalurak.{Server, Connection}
import scalurak.Connection.UserPacket

object ExampleApp {
    type Command = Server.Event

    def apply(): Behavior[Command] = Behaviors.setup(context => new ExampleApp(context))
}

/**
 * Basic implementation of an actor that would use a scalurak serever
 */
class ExampleApp(context: ActorContext[ExampleApp.Command]) extends AbstractBehavior[ExampleApp.Command](context) {
    import ExampleApp._
    // Create the scalurak server as a child actor
    context.spawn(Server(context.self, new InetSocketAddress("localhost", 1001)), "scalurak-server")

    def onMessage(msg: Command): Behavior[Command] = {
        msg match {
            // When we get a new connection, we tell it to use a new ConsumerHandler as the actor
            // to send all the events from the connection to
            case Server.Connected(remoteAddress, localAddress, connection) =>
                connection ! Connection.Register(context.spawn(AppUser(), s"connection-${remoteAddress.toString}"))
            // If something goes terribly wrong, die (this should only happen if we can't bind the TCP or UDP for some reason)
            case Server.CommandFailed => return Behaviors.stopped
        }
        this
    }
}
