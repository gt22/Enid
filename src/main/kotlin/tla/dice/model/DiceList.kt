package tla.dice.model

import kotlin.random.Random

data class DiceList(val dice: List<IDiceExpr>) : IDiceExpr {
    override val value: Int
        get() = dice.sumOf { it.value }

    override fun rollSimple(r: Random) {
        dice.forEach { it.rollSimple(r) }
    }

    override fun getDice() = dice.asSequence().flatMap { it.getDice() }

    override fun valueStr(): String {
        return "[${dice.joinToString(", ") { it.value.toString() }}]"
    }
}