package commands

import ReplyRef
import cmd.CommandCategory
import cmd.CommandContext
import cmd.CommandListBuilder
import cmd.ICommandList
import dsl.*
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.util.*

object FcCommands : ICommandList {
    override val cat = CommandCategory("Frangiclave", Color(0xA429FB), "http://52.48.142.75/frangiclave/static/images/elementArt/toolknockf.png")

    val editMap: MutableMap<Long, ReplyRef> = mutableMapOf()

    override fun init(): Init<CommandListBuilder> = {
        command("show") {
            help = "Show something from Frangiclave"
            args = "(%id%|%title%)"
            val arguments by parser.leftoverDelegate()
            action {
                show(arguments.joinToString(" ").trim(), EnumSet.allOf(CSManager.Sources::class.java))
            }
        }
        for(source in CSManager.Sources.values()) {
            val name = source.name.lowercase()
            command(name) {
                help = "Show $name from Frangiclave"
                args = "(%id%|%title%)"
                val arguments by parser.leftoverDelegate()
                action {
                    show(arguments.joinToString(" ").trim(), EnumSet.of(source))
                }
            }
        }
    }

    private fun CommandContext.show(arg: String, target: EnumSet<CSManager.Sources>) {
        val res = CSManager.createMessages(arg, true, target)
        if(message.idLong !in editMap) {
            sendReply(message, res) {
                editMap[message.idLong] = it
            }
        } else {
            editReply(message, res)
        }
    }

    private fun sendReply(
        to: Message,
        data: List<Pair<MessageEmbed, Attachments>>,
        blockPagination: Boolean = false,
        msgCallback: (Message) -> Unit = {},
        callback: (ReplyRef) -> Unit
    ) {
        if (!blockPagination && to.channel.acceptsPagination()) {
            to.channel.sendPaginatedEmbed(data.toList(), init = {
                mentionRepliedUser(false)
                reference(to)
            }) {
                callback(ReplyRef.Paginated(it))
            }
        } else {
            val msgs = Collections.synchronizedList(mutableListOf<Message>())
            for ((embed, attachments) in data) {
                attachments.asSequence().fold(to.replyEmbeds(embed)) { act, (name, stream) ->
                    act.addFile(stream, name)
                }
                    .mentionRepliedUser(false)
                    .queue {
                        msgs.add(it)
                        msgCallback(it)
                    }
            }
            callback(ReplyRef.Direct(msgs))
        }
    }

    private fun editReply(to: Message, newData: List<Pair<MessageEmbed, Attachments>>) {
        when (val rep = editMap[to.idLong]) {
            is ReplyRef.Direct -> {
                val newMsgs = Collections.synchronizedList(mutableListOf<Message>())
                newData.zip(rep.msgs).forEach { (edit, orig) ->
                    editEmbedWithAttachments(orig, edit).queue()
                    newMsgs.add(orig)
                }
                //At most one of these two actions has any effect, because shortest sequence will be empty after drop
                sendReply(to, newData.asSequence().drop(rep.msgs.size).toList(), blockPagination = true, msgCallback = {
                    newMsgs.add(it)
                }) {}
                rep.msgs.asSequence().drop(newData.size).forEach { it.delete().queue() }

                editMap[to.idLong] = ReplyRef.Direct(newMsgs)
            }
            is ReplyRef.Paginated -> {
                Reactions.clear(rep.msg.id)
                clearPaginatedEmbed(rep.msg)
                editToPaginatedEmbed(rep.msg, newData.toList())
            }
            null -> sendReply(to, newData) { editMap[to.idLong] = it }
        }
    }
}