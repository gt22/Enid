package cmd

import dsl.Init
import dsl.PaginatedEmbedCreator
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.requests.restaction.MessageAction

interface ICommandList {

    val cat: CommandCategory

    fun init(): Init<CommandListBuilder>


    fun CommandContext.replyCat(
        init: Init<MessageAction> = {},
        callback: (Message) -> Unit = {},
        embed: PaginatedEmbedCreator.() -> Unit
    ) = reply(init, callback) {
        pattern {
            thumbnail = cat.img
            color = cat.color
        }
        embed()
    }

}