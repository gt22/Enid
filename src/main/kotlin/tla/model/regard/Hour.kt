package tla.model.regard

import org.apache.commons.text.similarity.LevenshteinDistance
import javax.imageio.ImageIO
import kotlin.random.Random

data class Hour(val num: Int, val name: String, val enactments: List<String>) {
    val image by lazy { ImageIO.read(javaClass.getResourceAsStream("/tarot/$num.jpg")) }

    val imageName = "hour$num.jpg"

    val displayName
        get() = "The $name"

    companion object {

        private val distance = LevenshteinDistance(5)

        val hours = mapOf(
            0 to Hour(
                0,
                "Moth",
                listOf("who beats within the skull", "who is dappled", "who seeks among the trees of the Wood")
            ),
            1 to Hour(1, "Watchman", listOf("who navigates", "who illuminates", "who is not compassionate")),
            2 to Hour(2, "Velvet", listOf("who knows", "who knows?")),
            3 to Hour(3, "Malachite", listOf("who is succulent", "who encompasses", "who is renewed")),
            4 to Hour(
                4,
                "Thunderskin",
                listOf("who cannot be stilled", "demands the dance", "who is beaten, like a drum")
            ),
            5 to Hour(
                5,
                "Mother of Ants",
                listOf(
                    "who encircles",
                    "who opens",
                    "who arises from wounds",
                    "who spares those who are already harmed"
                )
            ),
            6 to Hour(
                6,
                "Witch and Sister",
                listOf("who cannot be touched", "who cannot be separated", "who are pearl")
            ),
            7 to Hour(7, "Colonel", listOf("who is blind", "who is scarred", "who cannot be denied")),
            8 to Hour(8, "Lionsmith", listOf("who is stronger", "who is seamless", "who makes monsters")),
            9 to Hour(
                9,
                "Elegiast",
                listOf(
                    "who calls each of the Dead by name",
                    "who cannot be deceived",
                    "from whom nothing more can be taken"
                )
            ),
            10 to Hour(
                10,
                "Beachcomber",
                listOf("who sees what is lost", "who opens the earth", "to whom belongs what is not possessed")
            ),
            11 to Hour(
                11,
                "Meniscate",
                listOf("from whom we do not turn", "who is exposed", "whose beauty is unmatched")
            ),
            12 to Hour(12, "Sun in Rags", listOf("which is distant", "which is not as it was", "which burns still")),
            13 to Hour(
                13,
                "Horned Axe",
                listOf(
                    "who bears two blades",
                    "who waits at the threshold",
                    "who permits passage when passage is to be permitted"
                )
            ),
            14 to Hour(
                14,
                "Madrugad",
                listOf("which goes before the Sun", "who quells and quiets", "who cannot be unbalanced")
            ),
            15 to Hour(15, "Red Grail", listOf("who gives life", "who takes life", "who is not sated")),
            16 to Hour(16, "Wolf Divided", listOf("who unmaketh", "who unmaketh", "who unmaketh")),
            17 to Hour(
                17,
                "Vagabond",
                listOf(
                    "who tells her tale to the willing and the unwilling",
                    "who cannot be stayed",
                    "who cannot be exhausted"
                )
            ),
            18 to Hour(
                18,
                "Sister and Witch",
                listOf("who cannot be separated", "who cannot be touched", "who are pearl")
            ),
            19 to Hour(
                19,
                "Flowermaker",
                listOf("who cannot touch you", "who cannot find you", "who always has what you desire")
            ),
            20 to Hour(
                20,
                "Forge of Days",
                listOf("who remakes with fire", "who ends what will not change", "who ends all nights")
            ),
            25 to Hour(25, "Crowned Growth", listOf()),
            27 to Hour(
                27,
                "Mare in the Tree",
                listOf("who dwelleth in the earth", "who sends up gifts", "who art the mare who destroyeth in mercy")
            )
        )

        fun random(r: Random): Hour {
            return hours.values.random(r)
        }

        fun byNum(num: Int): Hour {
            require(num in hours)
            return hours[num]!!
        }

        fun byName(name: String): List<Hour> {
            val normalized = name.trim().lowercase().removePrefix("the")
            val candidates = hours.values.asSequence()
                .map { h -> h to distance.apply(h.name.lowercase(), normalized) }
                .filter { it.second >= 0 }
                .sortedBy { it.second }
                .toList()
            return when(candidates.size) {
                0 -> emptyList()
                1 -> listOf(candidates[0].first)
                else -> if(candidates[0].second + 3 < candidates[1].second) listOf(candidates[0].first) else candidates.take(5).map { it.first }
            }
        }

        fun byStrictName(name: String) = hours.values.first { it.name == name }
    }

}