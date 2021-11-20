import CSManager.Sources.*
import dawnbreaker.data.raw.*
import dawnbreaker.vanilla
import dsl.Attachments
import dsl.BaseEmbedCreater
import dsl.Init
import dsl.embed
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.util.*
import kotlin.math.abs

object CSManager {

    private val data = vanilla
    private val baseUrl = "http://52.48.142.75/frangiclave"

    private data class PaginationData(
        val addPagination: Boolean,
        val pos: Int,
        val size: Int
    )

    fun createMessages(arg: String, usePagination: Boolean, target: EnumSet<Sources>): List<Pair<MessageEmbed, Attachments>> {
        val data = find(arg, true, target).take(5).ifEmpty { find(arg, false, target).take(5) }.toList()
        return data
            .mapIndexed { i, d ->
                createMessage(d, arg, PaginationData(usePagination && data.size > 1, i, data.size))
            }.ifEmpty { listOf(error(arg)) }
    }

    enum class Sources {
        DECK,
        ELEMENT,
        ENDING,
        LEGACY,
        RECIPE,
        VERB
    }

    private fun createMessage(d: Data, arg: String, pag: PaginationData): Pair<MessageEmbed, Attachments> = when (d) {
        is Deck -> deck(d, pag)
        is Element -> element(d, pag)
        is Ending -> ending(d, pag)
        is Legacy -> legacy(d, pag)
        is Recipe -> recipe(d, pag)
        is Verb -> verb(d, pag)
        else -> error(arg)
    }

    private fun find(arg: String, strict: Boolean, target: EnumSet<Sources>) = sequence {
        for(source in target) {
            when(source) {
                DECK -> {
                    yieldAll(find(arg, data.decks, strict))
                    if (arg.startsWith("internal:")) {
                        yieldAll(find(arg.removePrefix("internal:"), data.recipes, strict)
                            .mapNotNull { it.internaldeck?.apply { this.id = "internal:${it.id}" } })
                    }
                }
                ELEMENT -> yieldAll(find(arg, data.elements, strict))
                ENDING -> yieldAll(find(arg, data.endings, strict))
                LEGACY -> yieldAll(find(arg, data.legacies, strict))
                RECIPE -> yieldAll(find(arg, data.recipes, strict))
                VERB -> yieldAll(find(arg, data.verbs, strict))
                null -> assert(false)
            }
        }
    }

    private fun <T : Data> find(arg: String, from: List<T>, strict: Boolean) = sequence {
        yieldAll(from.asSequence().filter { it.id == arg })
        if (!strict) {
            yieldAll(from.asSequence().filter { getLabel(it)?.contains(arg, ignoreCase = true) ?: false })
        }
    }

    private fun base(d: Data, pag: PaginationData, init: Init<BaseEmbedCreater>) = embed {
        color = Color(0xA429FB)
        title = formatData(d) + if (pag.addPagination) " [${pag.pos + 1}/${pag.size}]" else ""
        url = "$baseUrl/${d.javaClass.simpleName.lowercase()}/${d.id}"
        thumbnail = getImageUrl(d)
        footer {
            text = "Cultist Simulator is the sole property of Weather Factory. All rights reserved.\n" +
                    "All game content in this embed and behind the link, including images and text, is used with permission."
        }
        init()
    }

    private fun deck(d: Deck, pag: PaginationData) = base(d, pag) {
        +d.description
        if (d.drawmessages.isNotEmpty()) {
            for ((elem, msg) in d.drawmessages) {
                append field formatData<Element>(elem) to msg
            }
        } else {
            append field "Contents" to formatElements(d.spec)
        }
        append field "When empty" to if (d.resetonexhaustion) "Resets" else "Always gives ${formatData<Element>(d.defaultcard)}"

    }

    private fun element(d: Element, pag: PaginationData) = base(d, pag) {
        +d.description
        append field "Type" to (if (d.isAspect) "Aspect" else "Card")
        if (d.aspects.isNotEmpty()) {
            append field "Aspects" to formatElements(d.aspects)
        }
        if (d.lifetime != 0) {
            append field "Decays..." to "In ${d.lifetime}s${if (d.decayTo.isNotEmpty()) ", into ${d.decayTo}" else ""}"
        } else if (d.decayTo.isNotEmpty()) {
            append field "Purges..." to "into ${d.decayTo}"
        }
        if (d.unique) {
            append field "Unique" to "Yes"
        } else if (d.uniquenessgroup.isNotEmpty()) {
            append field "Unique" to "Yes (${d.uniquenessgroup})"
        }
        if (d.induces.isNotEmpty()) {
            append field "Inductions" to d.induces.joinToString("\n") {
                "${formatData<Recipe>(it.id)} ${it.chance}%"
            }
        }
        if (d.xtriggers.isNotEmpty()) {
            append field "XTriggers" to d.xtriggers.asSequence().joinToString("\n") { (catalyst, triggers) ->
                "**${formatData<Element>(catalyst)} ->**\n ${
                    triggers.joinToString("\n") {
                        if ((it.morpheffect == "transform" || it.morpheffect == "") && it.id == d.id) {
                            "Refreshes lifetime"
                        } else {
                            when (it.morpheffect) {
                                "spawn" -> "Spawns "
                                "transform", "" -> "Transforms into "
                                "mutate" -> if (it.level >= 0) "Mutates additional " else "Mutates away "
                                else -> "Unknown"
                            } + "${formatElements(mapOf(it.id to if (it.level != 0) abs(it.level) else 1))}${if (it.chance != 0) " ${it.chance}%" else ""}"
                        }
                    }
                }"
            }
        }
    }

    private fun ending(d: Ending, pag: PaginationData) = base(d, pag) {
        +d.description

        inline field "Flavour" to d.flavour
        inline field "Animation" to d.anim
    }

    private fun legacy(d: Legacy, pag: PaginationData) = base(d, pag) {
        +"[Menu]\n"
        +d.description
        +"\n[In-Game]\n"
        +d.startdescription
        append field "Effects" to formatElements(d.effects)
        if (d.fromending.isNotEmpty()) {
            append field
                    "${if (d.availableWithoutEndingMatch) "Guaranteed" else "Only"} after Ending" to
                    formatData<Ending>(d.fromending)
        }
        if (d.excludesOnEnding.isNotEmpty()) {
            append field "Unavailable after Ending" to
                    d.excludesOnEnding.joinToString("\n") { formatData<Ending>(it) }
        }
    }

    private fun recipe(d: Recipe, pag: PaginationData) = base(d, pag) {
        if (d.startdescription.isNotEmpty()) {
            +"[Start]\n"
            +d.startdescription
        }
        if (d.description.isNotEmpty()) {
            if (d.startdescription.isNotEmpty()) {
                +"\n"
            }
            +"[End]\n"
            +d.description
        }

        if (d.burnimage.isNotEmpty()) {
            thumbnail = "$baseUrl/static/images/burnImages/${d.burnimage}.png"
        }

        if (d.requirements.isNotEmpty()) {
            append field "Requires" to formatElementsNamed(d.requirements)
        }
        if (d.tablereqs.isNotEmpty()) {
            append field "Requires (On Table)" to formatElements(d.tablereqs)
        }
        if (d.extantreqs.isNotEmpty()) {
            append field "Requires (Anywhere)" to formatElements(d.extantreqs)
        }
        if (d.effects.isNotEmpty()) {
            append field "Effects" to formatElementsNamed(d.effects, false)
        }
        if (d.deckeffects.isNotEmpty() || d.internaldeck != null) {
            append field "Deck draws" to d.deckeffects.asSequence()
                .map { (deck, count) -> deck to count }
                .plus(if (d.internaldeck != null) sequenceOf("internal:${d.id}" to d.internaldeck!!.draws) else emptySequence())
                .joinToString("\n") { (deck, count) ->
                    "${formatCount(count.toString())}${formatData<Deck>(deck)}"
                }
        }
        if (d.aspects.isNotEmpty()) {
            append field "Catalysts" to formatElements(d.aspects, false)
        }

        if (d.mutations.isNotEmpty()) {
            append field "Mutations" to d.mutations.joinToString("\n") {
                "${
                    formatElement(
                        it.filter,
                        null
                    )
                } -> ${if (it.additive) if (it.level > 0) "+" else "-" else ""}${
                    formatElement(
                        it.mutate,
                        if (it.additive) abs(it.level) else it.level,
                        reduce = false
                    )
                }"
            }
        }
        inline field "Duration" to if (d.warmup > 0) "${d.warmup}s" else "Instant"
        inline field "Type" to when {
            d.craftable -> "Craftable"
            d.hintonly -> "Hint"
            else -> "Unavailable"
        }
        inline field "Verb" to d.actionid
    }

    private fun verb(d: Verb, pag: PaginationData) = base(d, pag) {
        +d.description
    }

    private fun error(arg: String) = embed {
        color = Color(0xA429FB)
        title = "There's no such thing as $arg"
        thumbnail = "$baseUrl/static/images/elementArt/_x.png"
        footer {
            text = "Cultist Simulator is the sole property of Weather Factory. All rights reserved.\n" +
                    "All game content in this embed and behind the link, including images and text, is used with permission."
        }
    }

    private fun formatElements(elems: Map<String, Int>, reduce: Boolean = true): String {
        return elems.asSequence().sortedByDescending { (_, count) -> count }.joinToString("\n") { (id, count) ->
            formatElement(id, count, reduce)
        }
    }

    private fun formatElementsNamed(elems: Map<String, String>, reduce: Boolean = true): String {
        return elems.asSequence()
            .sortedWith { (_, a), (_, b) ->
                val ai = a.toIntOrNull()
                val bi = b.toIntOrNull()
                if (ai != null && bi != null) {
                    Integer.compare(bi, ai)
                } else if (bi == null) {
                    -1
                } else if (ai == null) {
                    1
                } else {
                    a.compareTo(b)
                }
            }
            .joinToString("\n") { (id, count) ->
                formatElement(id, if (!reduce || count != "1") count else null, reduce)
            }
    }

    private fun formatElements(elems: List<String>, reduce: Boolean = true): String {
        return elems.joinToString("\n") { id ->
            formatElement(id, null, reduce)
        }
    }

    private fun formatElement(id: String, count: Int, reduce: Boolean = true) =
        formatElement(id, if (!reduce || count != 1) count.toString() else null, reduce)

    private fun formatElement(id: String, count: String?, reduce: Boolean = true): String =
        "${if (count != null) formatCount(count, reduce) else ""}${formatData<Element>(id)}"

    private fun formatCount(count: String, reduce: Boolean = true): String {
        val ci = count.toIntOrNull()
        return if (ci != null) {
            if (reduce) {
                if (ci < 0) {
                    if (ci == -1) {
                        "No "
                    } else {
                        "Less than ${abs(ci)} × "
                    }
                } else {
                    "$ci × "
                }
            } else {
                "$ci × "
            }
        } else {
            formatElement(count, null) + " × "
        }
    }

    private inline fun <reified T : Data> formatData(id: String): String {
        if (T::class.java == Deck::class.java && id.startsWith("internal:")) {
            val r = data.lookup<Recipe>(id.removePrefix("internal:"))
            return formatLabel(r?.internaldeck?.label?.ifEmpty { null } ?: "Internal Deck", id)
        }
        val d = data.lookup<T>(id)
        return formatData(d, id)
    }

    private fun formatData(d: Data) = formatData(d, d.id)

    private fun getLabel(d: Data?) = when (d) {
        is Deck -> d.label
        is Element -> d.label
        is Ending -> d.label
        is Legacy -> d.label
        is Recipe -> d.label
        is Verb -> d.label
        else -> null
    }

    private fun formatData(d: Data?, id: String): String {
        return formatLabel(getLabel(d), id)
    }

    private fun formatLabel(label: String?, id: String) =
        if (label?.isEmpty() == true) id else "${label ?: "Unknown"} ($id)"

    private fun getImageUrl(d: Data): String? {
        val path = when (d) {
            is Element -> if (d.isAspect) "icons40/aspects" else "elementArt"
            is Ending -> "endingArt"
            is Legacy -> "icons100/legacies"
            is Verb -> "icons100/verbs"
            else -> null
        } ?: return null
        val icon = when (d) {
            is Element -> d.icon
            is Ending -> d.image
            is Legacy -> d.image
            else -> null
        }?.ifEmpty { null } ?: d.id
        return "$baseUrl/static/images/$path/${icon}.png"
        //TODO: Handle missing images
    }
}