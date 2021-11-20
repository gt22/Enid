package tla.dice.model

import kotlin.random.Random

data class BasicDice(val dice: Int) : IDiceExpr {

    override var value = 0

    override fun rollSimple(r: Random) {
        value = r.nextInt(1, dice + 1)
    }

    override fun getDice() = sequenceOf(this)

    override fun valueStr(): String {
        return "[$value]"
    }
}