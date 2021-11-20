package tla

import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import tla.model.TLAState

object TLAManager {

    private val games: MutableMap<Long, TLAState> = mutableMapOf()

    fun MessageChannel.startTLA() {
        check(idLong !in games) { "Game is already running in this channel" }
        games[idLong] = TLAState()
    }

    val MessageChannel.tla
        get() = games[idLong]

    fun MessageChannel.endTLA() {
        check(idLong in games) { "No game in this channel" }
        games.remove(idLong)
    }
}