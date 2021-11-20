package tla.dice.model

import kotlin.random.Random

data class Binop(val d1: IDiceExpr, val d2: IDiceExpr, val name: String, val op: (Int, Int) -> Int) : IDiceExpr {
    override val value: Int
        get() = op(d1.value, d2.value)

    override fun rollSimple(r: Random) {
        d1.rollSimple(r)
        d2.rollSimple(r)
    }

    override fun getDice() = d1.getDice() + d2.getDice()

    override fun valueStr(): String {
        val d1s = if(usePars(d1)) "(${d1.valueStr()})" else d1.valueStr()
        val d2s = if(usePars(d2)) "(${d2.valueStr()})" else d2.valueStr()
        return "$d1s$name$d2s"
    }

    private fun usePars(d: IDiceExpr): Boolean {
        return !(d is Constant || d is BasicDice || d is DiceList || (d is Binop && priority(d.name) > priority(name)))
    }

    private fun priority(o: String) = when(o) {
        "+", "-" -> 1
        "*", "/" -> 2
        "#" -> 3
        else -> 0
    }

}