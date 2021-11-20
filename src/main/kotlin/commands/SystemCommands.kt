package commands

import cmd.CommandCategory
import cmd.CommandListBuilder
import cmd.ICommandList
import dsl.Init
import java.awt.Color

object SystemCommands : ICommandList {
    override val cat: CommandCategory
        get() = CommandCategory("System", Color(0xA429FB), "http://52.48.142.75/frangiclave/static/images/elementArt/enid_c.png")

    override fun init(): Init<CommandListBuilder> = {
        command("help") {
            help = "Get some help about commands"
            args = "(%command%|args)+"
            action {
                if (args.isNotEmpty()) {
                    reply {
                        val cmdsNotFound = mutableListOf<String>()
                        args.forEach { name ->
                            if (name == "args") {
                                page {
                                    title = "Info about arguments notation"
                                    color = cat.color
                                    thumbnail = cat.img
                                    append field "%name%" to "Text argument. Like %command% in here corresponds to name of some command"
                                    append field "text" to "Constant text. Like 'args' in here means that you have to type 'args' to get this message"
                                    append field "i%name%" to "Number argument. Just some number, like 1, 12, or 6741"
                                    append field "(%arg%)+" to "Arguments with + on the end can be repeated infinitely, use space as separator"
                                    append field "(%arg%)?" to "Arguments with ? are optional, you can just skip them"
                                    append field "--some-flag" to "Flag argument. It's like constant text but with few features"
                                    append field "-(-s)ome-flag" to "Flag with shortname. You can use '-s' instead of '--some-flag'"
                                    append field "-(-s)ome-flag -(-o)ther-flag" to "Shortnames can be combined, so you can write both flags as '-so'"
                                    append field "r%from:to%" to "Range argument. Two numbers separated by :. You can skip one or both numbers. Usually that would mean 'to start/end'"
                                    append field "--some-arg=%some-value%" to "Named args. It's like constant text, then '=' and then text arg"
                                    append field "(%arg1%|%arg2%)" to "OR-type args. You can use one of them. If this is repeated, you can use both, just separate by space"
                                }
                            } else {
                                val cmd = Enid.commandClient[name]
                                if(cmd == null) {
                                    cmdsNotFound.add(name)
                                } else {
                                    page {
                                        title = name
                                        color = cmd.category.color
                                        thumbnail = cmd.category.img
                                        append field "${Enid.commandClient.prefix} $name ${cmd.args}" to cmd.help
                                    }
                                }
                            }
                        }
                        if(cmdsNotFound.isNotEmpty()) {
                            page {
                                title = "I'm sorry, i couldn't find those commands"
                                color = cat.color
                                thumbnail = cat.img
                                +cmdsNotFound.joinToString(", ")
                            }
                        }
                    }
                } else {
                    var curCat: CommandCategory? = null
                    reply {
                        Enid.commandClient.commands.forEach { name, cmd ->
                            if (!cmd.hidden) {
                                if (cmd.category != curCat) {
                                    if (curCat != null) {
                                        breakPage()
                                    }
                                    curCat = cmd.category
                                    color = cmd.category.color
                                    title = cmd.category.name
                                    thumbnail = cmd.category.img
                                }
                                append field "${Enid.commandClient.prefix} $name ${cmd.args}" to cmd.help
                            }
                        }
                    }
                }
            }
        }
    }
}
