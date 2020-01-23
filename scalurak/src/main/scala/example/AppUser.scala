package scalurak.example

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, AbstractBehavior}
import scalurak.Connection

object AppUser {
    type Command = Connection.Event

    def apply(): Behavior[Connection.Event] = Behaviors.setup(context => new AppUser(context))
}

/*
 * Basic implementation of an actor that handles events from a given scalurak connection
 */
class AppUser(context: ActorContext[AppUser.Command]) extends AbstractBehavior[AppUser.Command](context) {
    import AppUser._

    def onMessage(msg: Command): Behavior[Command] = {
        msg match {
            // The client sent us some data (that's not internal to the protocol)
            case Connection.UserPacket(data) => ()
        }
        this
    }
}
