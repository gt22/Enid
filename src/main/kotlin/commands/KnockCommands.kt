package commands

import cmd.CommandCategory
import cmd.CommandListBuilder
import cmd.ICommandList
import cmd.canWrite
import dsl.Init
import dsl.editToPaginatedEmbed
import dsl.sendPaginatedEmbed
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.TextChannel
import java.awt.Color
import java.awt.Color.RED

object KnockCommands : ICommandList {
    override val cat = CommandCategory("Knock", Color(0xA429FB), "http://52.48.142.75/frangiclave/static/images/icons40/aspects/knock.png")

    override fun init(): Init<CommandListBuilder> = {
        command("portal") {
            help = "Open a portal to another channel"
            args = "%channel% (%theme%)?"

            val channel by parser.plain("channel")
            val theme by parser.leftoverDelegate()

            action {
                val ch = channel.value ?: return@action replyCat {
                    color = RED
                    title = "Channel name required"
                }
                val target = if(ch.matches("<#\\d+>".toRegex())) {
                    val chId = ch.removePrefix("<#").removeSuffix(">")
                    guild.getGuildChannelById(chId) as? TextChannel
                } else {
                    guild.getTextChannelsByName(ch, true).firstOrNull()
                }
                if(target == null) {
                    return@action replyCat {
                        color = RED
                        title = "Can't find channel $ch"
                    }
                }
                if(!target.canWrite()) {
                    return@action replyCat {
                        color = RED
                        title = "Can't open portal to ${target.name}"
                        thumbnail = cat.img
                    }
                }
                if(!target.canWrite(member)) {
                    return@action replyCat {
                        color = RED
                        title = "The passage is not permitted"
                        thumbnail = "http://52.48.142.75/frangiclave/static/images/elementArt/op.horned.png"
                    }
                }
                val th = theme.joinToString(" ")
                replyCat(callback = { source ->
                    target.sendPaginatedEmbed(callback = { sink ->
                        editToPaginatedEmbed(source) {
                            title = "Portal to ${target.name}"
                            url = sink.jumpUrl
                            color = cat.color
                            thumbnail = cat.img
                            +th
                        }
                    }) {
                        title = "Portal from ${message.channel.name}"
                        url = source.jumpUrl
                        color = cat.color
                        thumbnail = cat.img
                        +th
                    }
                }) {
                    title = "Opening a portal to ${target.name}..."
                }
            }
        }
    }
}