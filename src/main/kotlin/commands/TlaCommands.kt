package commands

import cmd.CommandCategory
import cmd.CommandListBuilder
import cmd.ICommandList
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import dsl.BaseEmbedCreater
import tla.dice.DiceParser
import dsl.Init
import tla.TLAManager.endTLA
import tla.TLAManager.startTLA
import tla.TLAManager.tla
import tla.dice.ImageMaker
import tla.dice.ImageMaker.toByeArray
import tla.model.regard.Hour
import java.awt.Color
import java.lang.IllegalStateException
import kotlin.random.Random

object TlaCommands : ICommandList {

    override val cat = CommandCategory(
        "The Lady Afterwards",
        Color(0xFF6800),
        "http://52.48.142.75/frangiclave/static/images/icons40/aspects/forge.png"
    )

    override fun init(): Init<CommandListBuilder> = {
        command("roll") {
            aliases {
                +"r"
            }
            help = "Roll dices (default d10#d10)\n"
            args = "%expr%"
            val exprL by parser.leftoverDelegate()
            val d100 = DiceParser.parseToEnd("d10#d10")
            action {
                val expr = exprL.joinToString(" ").let { if (it.isBlank()) d100 else DiceParser.parseToEnd(it) }
                replyCat {
                    title = "Total: ${expr.roll(Random)}"
                    thumbnail = "die${expr.value}.png" attach ImageMaker.write(expr.value).toByeArray()
                    color = cat.color
                    +expr.valueStr()
                }
            }
        }
        command("start") {
            help = "Start TLA game"
            action {
                try {
                    message.channel.startTLA()
                    replyCat {
                        color = cat.color
                        title = "A new game has been started"
                    }
                } catch (e: IllegalStateException) {
                    replyCat {
                        color = Color.RED
                        title = "Something went wrong"
                        +e.localizedMessage
                    }
                }
            }
        }
        command("end") {
            help = "Stop TLA game (permanently)"
            action {
                try {
                    message.channel.endTLA()
                    replyCat {
                        color = cat.color
                        title = "A game has been ended"
                    }
                } catch (e: IllegalStateException) {
                    replyCat {
                        color = Color.RED
                        title = "Something went wrong"
                        +e.localizedMessage
                    }
                }
            }
        }
        command("regard") {
            help = "Manage Regard of Hours"
            action {
                val tla = message.channel.tla ?: return@action replyCat {
                    title = "The game isn't running"
                    color = Color.RED
                    +"There's no game running in this channel, use '${Enid.commandClient.prefix} start' to start one"
                }
                if (args.isEmpty()) { // Show
                    replyCat {
                        tla.regard.regard.forEach { (hour, dice) ->
                            page {
                                title = "${hour.displayName}'s Regard"
                                thumbnail = attach(hour)
                                append field "Dice" to dice.sorted().joinToString(", ") { "d$it" }
                            }
                        }
                        if(tla.regard.freeDice.isNotEmpty()) {
                            page {
                                title = "Used dice"
                                append field "Dice" to tla.regard.freeDice.sorted().joinToString(", ") { "d$it" }
                            }
                        }
                    }
                } else when (args[0]) {
                    "clean" -> {
                        val withdrawn = tla.regard.clean()
                        replyCat {
                            title = "Regard has been normalized"
                            if (withdrawn.isNotEmpty()) {
                                +"${withdrawn.size} hours withdrew from the events"
                            }
                        }
                    }
                    "next" -> {
                        if (tla.regard.regard.size == Hour.hours.size) {
                            return@action replyCat {
                                title = "All hours are already paying attention"
                            }
                        }
                        var next: Hour
                        do {
                            next = Hour.random(Random)
                        } while (next in tla.regard.regard)

                        tla.regard.place(next)
                        replyCat {
                            title = "${next.displayName} now observes the events"
                            thumbnail = attach(next)
                            +"The Hour is now influencing the events"
                        }
                    }
                    else -> {
                        if (args.size < 3) {
                            return@action replyCat {
                                title = "Please specify Hour and the die"
                                color = Color.RED
                            }
                        }
                        selectHour(args[1]) { hour ->
                            val die = args[2].removePrefix("d").toIntOrNull() ?: return@action replyCat {
                                title = "Can't read '${args[2]}' as a die"
                                color = Color.RED
                            }
                            when (args[0]) {
                                "put" -> {
                                    if (tla.regard.addDie(hour, die)) {
                                        replyCat {
                                            title = "${hour.displayName} now observes the events"
                                            thumbnail = attach(hour)
                                            +"d$die has beed added to ${hour.displayName}'s regard, and the Hour is now influencing the events"
                                        }
                                    } else {
                                        replyCat {
                                            title = "Attention of ${hour.displayName} has increased"
                                            thumbnail = attach(hour)
                                            +"d$die has been added to ${hour.displayName}'s regard"
                                        }
                                    }
                                }
                                "take", "remove" -> {
                                    if (tla.regard.takeDie(hour, die, args[0] == "take")) {
                                        if (hour in tla.regard.regard) {
                                            replyCat {
                                                title = "Attention of ${hour.displayName} has decreased"
                                                thumbnail = attach(hour)
                                                +"d$die has been taken from ${hour.displayName}'s regard"
                                            }
                                        } else {
                                            replyCat {
                                                title = "${hour.displayName} withdrew from the events"
                                                thumbnail = attach(hour)
                                                +"d$die has been taken from ${hour.displayName}'s regard, and there's no more dice on it"
                                            }
                                        }
                                    } else {
                                        replyCat {
                                            title = "No such die"
                                            thumbnail = attach(hour)
                                            +"There's no d$die die on ${hour.displayName}'s regard"
                                        }
                                    }
                                }
                                else -> replyCat {
                                    title = "Unknown command ${args[1]}"
                                    color = Color.RED
                                    +"Known commands are {put, take, clean, next}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun BaseEmbedCreater.attach(h: Hour) = h.imageName attach h.image.toByeArray()

    private inline fun selectHour(arg: String, callback: (Hour) -> Unit) {
        callback(arg.toIntOrNull()?.let {
            Hour.byNum(it)
        } ?: Hour.byName(arg).first())
    }

}