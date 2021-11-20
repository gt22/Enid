package cmd


typealias CommandAction = suspend CommandContext.() -> Unit
typealias CanPerformCheck = suspend CommandContext.() -> Boolean?

open class Command(
    val name: String, val aliases: List<String>, val category: CommandCategory,
    val args: String, val help: String, val hidden: Boolean, val canPerform: CanPerformCheck?,
    val onDenied: CommandAction? = null, val action: CommandAction
) {

    open suspend fun perform(context: CommandContext) {
        context.action()
    }

    open suspend fun deny(context: CommandContext) {
        onDenied?.invoke(context)
    }

    open suspend fun canPerform(context: CommandContext): Boolean {
        return canPerform?.invoke(context) ?: true
    }


}