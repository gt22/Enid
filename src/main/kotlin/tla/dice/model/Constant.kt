package tla.dice.model

import kotlin.random.Random

data class Constant(override val value: Int) : IDiceExpr {
    override fun rollSimple(r: Random) {}
    override fun getDice(): Sequence<BasicDice> = emptySequence()
    override fun valueStr() = value.toString()
}