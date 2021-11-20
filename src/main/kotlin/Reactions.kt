import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent

typealias ReactionHandler = (MessageReactionAddEvent) -> Boolean
object Reactions {


    private val handlers = mutableMapOf<String, MutableList<ReactionHandler>>()


    fun register(id: String, handler: ReactionHandler) = handlers.getOrPut(id, ::mutableListOf).add(handler)

    fun unregister(id: String, handler: ReactionHandler) = handlers[id]?.remove(handler)

    fun clear(id: String) = handlers[id]?.clear()

    @SubscribeEvent
    fun MessageReactionAddEvent.handle() {
        //Run all handlers, and remove ones that returns 'true'
        handlers[messageId]?.removeAll { handler -> handler(this) }
    }

}