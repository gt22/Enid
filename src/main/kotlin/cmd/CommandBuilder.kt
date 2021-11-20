@file:Suppress("REDUNDANT_NULLABLE")

package cmd

import argparser.ArgParser
import dsl.Init

@DslMarker
annotation class CommandBuilderDsl

@CommandBuilderDsl
class CommandBuilder {

    var name: String = ""
    var category: CommandCategory? = null
    private var _action: CommandAction = {}
    private var _onDenied: CommandAction? = null
    private var _canPerformCheck: CanPerformCheck? = null
    var args: String = ""
    var help: String = ""
    var hidden: Boolean = false
    var aliases = mutableListOf<String>()

    val parser by lazy { ArgParser() }

    fun action(a: CommandAction) {
        _action = {
            parser.with(args) {
                a()
            }
        }
    }

    fun onDenied(a: CommandAction) {
        _onDenied = a
    }

    fun canPerform(a: CanPerformCheck) {
        _canPerformCheck = a
    }

    fun aliases(init: Init<AliasesBuilder>) {
        val b = AliasesBuilder(this)
        b.init()
        aliases.addAll(b.aliases)
    }

    fun build() = Command(name, aliases,
        category ?: throw IllegalStateException("Category must be initialized"),
        args, help, hidden, _canPerformCheck, _onDenied, _action)

}

@CommandBuilderDsl
class AliasesBuilder(val b: CommandBuilder) {

    val aliases = mutableListOf<String>()

    operator fun String.unaryPlus() {
        aliases.add(this)
    }

}

@CommandBuilderDsl
class CommandListBuilder(val category: CommandCategory) {

    val cl = mutableListOf<Command>()

    fun command(name: String? = null, init: Init<CommandBuilder>) {
        val b = CommandBuilder()
        b.category = category
        if(name != null) {
            b.name = name
        }
        b.init()
        cl.add(b.build())
    }

}

fun createCommand(init: Init<CommandBuilder>): Command {
    val b = CommandBuilder()
    b.init()
    return b.build()
}

fun commandList(init: Init<CommandListBuilder>, category: CommandCategory): List<Command> {
    val b = CommandListBuilder(category)
    b.init()
    return b.cl
}