package cmd

import argparser.tokenize
import ifPrefixed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*

fun IPermissionContainer.canWrite(member: Member = guild.selfMember) = member.getPermissions(this).containsAll(listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS))

class CommandClient(val prefix: String) {

    enum class ExecutionResult {
        SUCCESS,
        NOT_FOUND,
        NOT_A_COMMAND,
        ERROR
    }

    private val _commands: MutableMap<String, Command> = mutableMapOf()

    val commands: Map<String, Command>
        get() = _commands

    private val _commandCache: MutableMap<String, CommandContext> = mutableMapOf()

    val commandCache: Map<String, CommandContext>
        get() = _commandCache

    operator fun get(name: String): Command? = commands[name]

    fun register(command: Command) {
        _commands[command.name] = command
        command.aliases.forEach { _commands[it] = command }
    }

    fun register(vararg commands: Command) {
        commands.forEach(::register)
    }

    fun register(commands: Iterable<Command>) {
        commands.forEach(::register)
    }

    fun register(commands: ICommandList) {
        register(commandList(commands.init(), commands.cat))
    }

    fun register(vararg commands: ICommandList) {
        commands.forEach(::register)
    }


    suspend fun handle(message: Message): Pair<ExecutionResult, String> {
        if(message.channel !is IPermissionContainer || !(message.channel as IPermissionContainer).canWrite()) {
            return ExecutionResult.NOT_A_COMMAND to message.channel.javaClass.simpleName
        }
        message.contentRaw.ifPrefixed(prefix, true) { contentClean ->
            val tokenized = tokenize(contentClean.trim())
            if (tokenized.isEmpty()) {
                return ExecutionResult.NOT_A_COMMAND to ""
            }
            val cmd = tokenized[0]
            val args = tokenized.subList(1, tokenized.size)
            val context = if (cmd == "!!") {
                commandCache[message.guild.id]?.copy(message)
                    ?: return ExecutionResult.NOT_FOUND to "First message in guild"
            } else {
                val command = this[cmd]
                    ?: return ExecutionResult.NOT_FOUND to cmd
                CommandContext(command, message, args)
            }
            return executeByContext(context)
        }
        return ExecutionResult.NOT_A_COMMAND to ""
    }

    private suspend fun executeByContext(context: CommandContext): Pair<ExecutionResult, String> {
        val command = context.command
        try {
            if (command.canPerform(context)) {
                command.perform(context)
            } else {
                command.deny(context)
            }
            _commandCache[context.guild.id] = context
        } catch (e: Exception) {
            return ExecutionResult.ERROR to "${e.javaClass.simpleName}: ${e.localizedMessage}"
        }
        return ExecutionResult.SUCCESS to ""
    }

}