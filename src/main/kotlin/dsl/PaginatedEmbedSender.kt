package dsl

import Enid
import Reactions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import java.util.concurrent.atomic.AtomicInteger

val actionReactions = listOf("\u23ee", "\u23ea", "\u23f9", "\u23e9", "\u23ed")

fun MessageChannel.sendEmbedWithAttachments(e: Pair<MessageEmbed, Attachments>): MessageAction {
    return e.second.asSequence().fold(
        sendMessageEmbeds(e.first)
    ) { act, (name, stream) -> act.addFile(stream, name) }
}

fun editEmbedWithAttachments(msg: Message, e: Pair<MessageEmbed, Attachments>): MessageAction {
    return e.second.asSequence().fold(
        msg.editMessageEmbeds(e.first)
    ) { act, (name, stream) -> act.addFile(stream, name) }.override(true)
}

fun MessageChannel.sendPaginatedEmbed(
    init: Init<MessageAction> = {},
    callback: (Message) -> Unit = {},
    embed: PaginatedEmbedCreator.() -> Unit
) {
    sendPaginatedEmbed(paginatedEmbed(embed), init, callback)
}

fun clearPaginatedEmbed(msg: Message) {
    actionReactions.forEach { msg.clearReactions(it).queue() }
}

fun MessageChannel.acceptsPagination() =
    (type in listOf(
        ChannelType.TEXT,
        ChannelType.GUILD_PUBLIC_THREAD,
        ChannelType.GUILD_PRIVATE_THREAD
    )) && (this as GuildChannel).guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)

fun handlePaginationReply(
    e: MessageReactionAddEvent,
    msg: Message,
    curPage: AtomicInteger,
    embeds: List<Pair<MessageEmbed, Attachments>>
): Boolean {
    if (e.user != Enid.bot.selfUser) {
        if (e.reactionEmote.isEmoji && e.reactionEmote.emoji in actionReactions) {
            var newPage = when (e.reactionEmote.name) {
                "\u23ee" -> 0
                "\u23ea" -> curPage.get() - 1
                "\u23f9" -> {
                    clearPaginatedEmbed(msg)
                    return true
                }
                "\u23e9" -> curPage.get() + 1
                "\u23ed" -> embeds.size - 1
                else -> curPage.get()
            }
            e.reaction.removeReaction(e.retrieveUser().complete()).queue()
            newPage = newPage.coerceIn(embeds.indices)
            if (curPage.get() != newPage) {
                curPage.set(newPage)
                editEmbedWithAttachments(msg, embeds[newPage]).queue()
            }
        }
    }
    return false
}

private fun MessageAction.queuePaginatedEmbed(
    embeds: List<Pair<MessageEmbed, Attachments>>,
    callback: (Message) -> Unit
) {
    queue { msg ->
        val curPage = AtomicInteger()
        Reactions.register(msg.id) {
            handlePaginationReply(it, msg, curPage, embeds)
        }
        GlobalScope.launch(Dispatchers.Default) {
            actionReactions.map(msg::addReaction).forEach { it.complete(); }
        }
        callback(msg)
    }
}

fun editToPaginatedEmbed(
    msg: Message,
    init: Init<MessageAction> = {},
    callback: (Message) -> Unit = {},
    embed: PaginatedEmbedCreator.() -> Unit
) {
    editToPaginatedEmbed(msg, paginatedEmbed(embed), init, callback)
}

fun editToPaginatedEmbed(
    msg: Message,
    embeds: List<Pair<MessageEmbed, Attachments>>,
    init: Init<MessageAction> = {},
    callback: (Message) -> Unit = {}
) {
    if (embeds.isEmpty()) {
        return
    }
    if (embeds.size == 1) {
        editEmbedWithAttachments(msg, embeds[0]).apply(init).queue(callback)
        return
    }
    editEmbedWithAttachments(msg, embeds[0]).apply(init).queuePaginatedEmbed(embeds, callback)
}

fun MessageChannel.sendPaginatedEmbed(
    embeds: List<Pair<MessageEmbed, Attachments>>,
    init: Init<MessageAction> = {},
    callback: (Message) -> Unit = {}
) {
    if (embeds.isEmpty()) {
        return
    }
    if (embeds.size == 1) {
        sendEmbedWithAttachments(embeds[0]).apply(init).queue(callback)
        return
    }
    sendEmbedWithAttachments(embeds[0]).apply(init).queuePaginatedEmbed(embeds, callback)
}