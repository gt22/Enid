import net.dv8tion.jda.api.entities.Message

sealed class ReplyRef {

    data class Paginated(val msg: Message) : ReplyRef()

    data class Direct(val msgs: List<Message>) : ReplyRef()

}