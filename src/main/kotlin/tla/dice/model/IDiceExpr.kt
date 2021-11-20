package tla.dice.model

import kotlin.random.Random

interface IDiceExpr {

    fun roll(r: Random): Int {
        rollSimple(r)
        return value
    }

    val value: Int

    fun rollSimple(r: Random)

    fun getDice(): Sequence<BasicDice>

    fun valueStr(): String
}