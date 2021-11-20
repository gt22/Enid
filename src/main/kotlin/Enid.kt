import cmd.CommandClient
import commands.FcCommands
import commands.KnockCommands
import commands.SystemCommands
import commands.TlaCommands
import dawnbreaker.loadVanilla
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

object Enid {

    lateinit var bot: JDA
        private set

    var lastBot: JDA? = null
        private set

    lateinit var commandClient: CommandClient

    @JvmStatic
    fun main(args: Array<String>) {
        loadVanilla(Paths.get("cs_content"))
        System.setProperty(
            "org.slf4j.simpleLogger.logFile",
            "System.out"
        ) //Redirect slf4j-simple to System.out from System.err
        commandClient = CommandClient("fc!")
        commandClient.register(SystemCommands, FcCommands, KnockCommands, TlaCommands)
        bot = JDABuilder.createDefault(
            Config.token,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.DIRECT_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
        )
            .apply {
                setEventManager(AnnotatedEventManager())
                addEventListeners(EventListener, Reactions)
                setActivity(Activity.watching("itself loading"))
                setStatus(OnlineStatus.DO_NOT_DISTURB)
            }.build()
        bot.awaitReady()
        while (true) {
            FcCommands.editMap.clear()
            val (activityType, msg) = Config.presence.random()
            bot.presence.setPresence(OnlineStatus.ONLINE, Activity.of(activityType, msg))
            Thread.sleep(TimeUnit.MINUTES.toMillis(5))
        }
    }

}