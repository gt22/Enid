import kotlinx.serialization.json.*
import net.dv8tion.jda.api.entities.Activity
import java.nio.file.Files
import java.nio.file.Paths

object Config {

    val token: String

    val presence: List<Pair<Activity.ActivityType, String>>
    val catalogueLinks: List<String>

    init {
        val cfg = Json.parseToJsonElement(Files.readString(Paths.get("config.json"))).jsonObject
        val tokenType = requireNotNull(cfg["tokenType"]).jsonPrimitive.content
        token = requireNotNull(cfg[tokenType + "_token"]).jsonPrimitive.content
        presence = sequence {
            val p = requireNotNull(cfg["presence"]).jsonObject
            yieldAll(parsePresence(p["playing"]?.jsonArray, Activity.ActivityType.PLAYING))
            yieldAll(parsePresence(p["listening"]?.jsonArray, Activity.ActivityType.LISTENING))
            yieldAll(parsePresence(p["watching"]?.jsonArray, Activity.ActivityType.WATCHING))

        }.toList()
        catalogueLinks = cfg["catalogue"]?.jsonArray?.asSequence()?.map { it.jsonPrimitive.content }?.toList() ?: emptyList()
    }

    private fun parsePresence(p: JsonArray?, a: Activity.ActivityType) = sequence {
        if(p != null) {
            for(m in p) {
                yield(a to m.jsonPrimitive.content)
            }
        }
    }

}