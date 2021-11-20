package tla.model.regard

data class RegardState(
    val regard: MutableMap<Hour, MutableList<Int>> = mutableMapOf(),
    val freeDice: MutableList<Int> = mutableListOf()
) {

    fun addDie(to: Hour, die: Int): Boolean {
        val ret = to !in regard
        regard.compute(to) { _, v -> (v ?: mutableListOf()).apply { add(die) } }
        return ret
    }

    fun takeDie(from: Hour, die: Int, rememberDie: Boolean = true): Boolean {
        val ret = regard[from]?.remove(die) ?: false
        if(ret) {
            if(rememberDie) {
                freeDice.add(die)
            }
            if(regard[from]?.size == 0) {
                regard.remove(from)
            }
        }
        return ret
    }

    fun clean(): List<Hour> {
        val ret = regard.asSequence().filter { (_, d) -> d.isEmpty() }.map { (h, _) -> h }.toList()
        ret.forEach { regard.remove(it) }
        return ret
    }

    fun place(h: Hour, addDice: Boolean = true): Boolean {
        return if(h in regard) {
            false
        } else {
            regard[h] = if(addDice) {
                if(freeDice.isNotEmpty()) {
                    val res = ArrayList(freeDice)
                    freeDice.clear()
                    res
                } else {
                    regard.filter { (_, dice) -> dice.isNotEmpty() }.maxByOrNull { (hour, _) -> hour.num }?.let { (hour, dice) ->
                        val die = dice.maxOrNull()!!
                        takeDie(hour, die, false)
                        mutableListOf(die)
                    } ?: mutableListOf()
                }
            } else {
                mutableListOf()
            }
            true
        }
    }

    companion object {

        fun default(): RegardState {
            return RegardState(mutableMapOf(
                Hour.byStrictName("Watchman") to mutableListOf(),
                Hour.byStrictName("Lionsmith") to mutableListOf(),
                Hour.byStrictName("Sun in Rags") to mutableListOf()
            ))
        }

    }

}