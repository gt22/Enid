import cmd.CommandClient
import dsl.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import java.awt.Color
import java.lang.Integer.min
import java.util.*

inline fun String.ifPrefixed(prefix: String, ignoreCase: Boolean = false, action: (String) -> Unit) {
    if (startsWith(prefix, ignoreCase)) {
        action(substring(prefix.length))
    }
}

object EventListener {

    @SubscribeEvent
    fun onMessage(e: MessageReceivedEvent) {
        onMsg(e.message)
    }

    @SubscribeEvent
    fun onMessageEdit(e: MessageUpdateEvent) {
        onMsg(e.message)
    }

    private fun onMsg(message: Message) {
        GlobalScope.launch {
            val (res, v) = Enid.commandClient.handle(message)
            when(res) {
                CommandClient.ExecutionResult.ERROR -> {
                    message.channel.sendEmbedWithAttachments(embed {
                        color = Color.RED
                        title = "Something went wrong"
                        append field "Message" to v
                    }).queue()
                }
                else -> {}
            }
        }
    }
}