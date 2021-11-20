package tla.dice.model

import kotlin.random.Random

data class Unop(val d1: IDiceExpr, val name: String, val op: (Int) -> Int) : IDiceExpr {
    override val value: Int
        get() = op(d1.value)

    override fun rollSimple(r: Random) {
        d1.rollSimple(r)
    }

    override fun getDice() = d1.getDice()

    override fun valueStr(): String {
        return "-(${d1.valueStr()})"
    }
}