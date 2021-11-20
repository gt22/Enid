package cmd

import dsl.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.requests.restaction.MessageAction
import java.io.File

typealias Success<T> = (T) -> Unit
typealias Failure = Success<Throwable>

class CommandContext(val command: Command, val message: Message, val args: List<String>) {

    val author = message.author

    val guild = message.guild

    val member: Member = guild.retrieveMember(author).complete()

    fun copy(byMessage: Message): CommandContext =
        CommandContext(command, byMessage, args)

    fun reply(msg: CharSequence,
              success: Success<Message>? = null, failure: Failure? = null)
            = message.channel.sendMessage(msg).queue(success, failure)

    fun reply(msg: Message,
              success: Success<Message>? = null, failure: Failure? = null)
            = message.channel.sendMessage(msg).queue(success, failure)

    fun reply(msg: Pair<MessageEmbed, Attachments>,
              success: Success<Message>? = null, failure: Failure? = null)
            = message.channel.sendEmbedWithAttachments(msg).queue(success, failure)

    fun reply(init: Init<MessageAction> = {},
              callback: (Message) -> Unit = {},
              embed: PaginatedEmbedCreator.() -> Unit) = message.channel.sendPaginatedEmbed(init, callback, embed)

}