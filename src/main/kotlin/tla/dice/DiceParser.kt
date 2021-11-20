package tla.dice

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.CharToken
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import tla.dice.model.*

object DiceParser : Grammar<IDiceExpr>() {

    private val dT by literalToken("d")
    private val numT by regexToken("\\d+")
    private val spaceT by regexToken("\\s+", ignore = true)

    private val plusT by literalToken("+")
    private val minusT by literalToken("-")
    private val mulT by literalToken("*")
    private val divT by literalToken("/")
    private val concatT by literalToken("#")

    private val lparT by literalToken("(")
    private val rparT by literalToken(")")

    private val num by numT use { text.toInt() }
    private val d by -optional(spaceT) * dT * -optional(spaceT)

    private val constant by num map { Constant(it) }
    private val dice by (-d * num) map { BasicDice(it) }
    private val ndice by (optional(num) * -d * num) map { (n, d) -> DiceList(List(n ?: 1) { BasicDice(d) }) }

    private val concat by ((dice or constant) * -concatT * (dice or constant)) map { (a, b) ->
        Binop(a, b, "#") { x, y ->
            when(a) {
                is Constant -> if(x == 10) y else x * 10 + y
                is BasicDice -> if(x == a.dice) y else x * 10 + y
                else -> throw IllegalArgumentException()
            }
        }
    }

    private val term: Parser<IDiceExpr> by
    concat or ndice or constant or
            (-minusT * parser(::term) map { Unop(it, "-") { v -> -v } }) or
            (-lparT * parser(::rootParser) * -rparT)


    private val mulDiv by leftAssociative(term, mulT or divT use { type }) { a, op, b ->
        Binop(a, b, (op as CharToken).text.toString()) { x, y ->
            when(op) {
                mulT -> x * y
                divT -> x / y
                else -> throw IllegalStateException("Invalid op: ${op.name}")
            }
        }
    }

    private val addSub by leftAssociative(mulDiv, plusT or minusT use { type }) { a, op, b ->
        Binop(a, b, (op as CharToken).text.toString()) { x, y ->
            when(op) {
                plusT -> x + y
                minusT -> x - y
                else -> throw IllegalStateException("Invalid op: ${op.name}")
            }
        }
    }

    override val rootParser: Parser<IDiceExpr>
        get() = addSub

}