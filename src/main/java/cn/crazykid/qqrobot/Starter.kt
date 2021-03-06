package cn.crazykid.qqrobot

import cc.moecraft.icq.PicqBotX
import cc.moecraft.icq.PicqConfig
import cc.moecraft.icq.command.interfaces.IcqCommand
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.logger.environments.ColorSupportLevel
import cn.crazykid.qqrobot.command.RollCommand
import cn.crazykid.qqrobot.config.PicqBotXConfig
import cn.crazykid.qqrobot.listener.HeartBeatListener
import cn.crazykid.qqrobot.listener.LocalExceptionListener
import cn.crazykid.qqrobot.listener.friend.FriendAddListener
import cn.crazykid.qqrobot.listener.friend.FriendMessageRecallListener
import cn.crazykid.qqrobot.listener.friend.FriendPokeListener
import cn.crazykid.qqrobot.listener.friend.message.FriendMessageListener
import cn.crazykid.qqrobot.listener.group.*
import cn.crazykid.qqrobot.listener.group.message.*
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
@MapperScan(value = ["cn.crazykid.qqrobot.mapper"])
@ConfigurationPropertiesScan
open class Starter : CommandLineRunner {
    @Autowired
    private lateinit var rollCommand: RollCommand

    @Autowired
    private lateinit var heartBeatListener: HeartBeatListener

    @Autowired
    private lateinit var localExceptionListener: LocalExceptionListener

    @Autowired
    private lateinit var friendMessageListener: FriendMessageListener

    @Autowired
    private lateinit var friendAddListener: FriendAddListener

    @Autowired
    private lateinit var friendPokeListener: FriendPokeListener

    @Autowired
    private lateinit var groupMessageHelpListener: GroupMessageHelpListener

    @Autowired
    private lateinit var friendMessageRecallListener: FriendMessageRecallListener

    @Autowired
    private lateinit var groupMessageCountListener: GroupMessageCountListener

    @Autowired
    private lateinit var groupMessageCounterListener: GroupMessageCounterListener

    @Autowired
    private lateinit var groupMessageBotAtListener: GroupMessageBotAtListener

    @Autowired
    private lateinit var groupMessageBotRepeatListener: GroupMessageBotRepeatListener

    @Autowired
    private lateinit var groupMessageBilibiliParserListener: GroupMessageBilibiliParserListener

    @Autowired
    private lateinit var groupMessageRecallListener: GroupMessageRecallListener

    @Autowired
    private lateinit var groupMessageRedPackListener: GroupMessageRedPackListener

    @Autowired
    private lateinit var groupMessageControlRecallListener: GroupMessageControlRecallListener

    @Autowired
    private lateinit var groupMessageGuessMaimaiSongListener: GroupMessageGuessMaimaiSongListener

    @Autowired
    private lateinit var groupMessageRandomMaimaiMusicListener: GroupMessageRandomMaimaiMusicListener

    @Autowired
    private lateinit var groupMessageMaimaiQueueCardListener: GroupMessageMaimaiQueueCardListener

    @Autowired
    private lateinit var groupMessageArcadeQueueListener: GroupMessageArcadeQueueListener

    @Autowired
    private lateinit var groupMessageRandomMaimaiArcadeListener: GroupMessageRandomMaimaiArcadeListener

    @Autowired
    private lateinit var groupPokeListener: GroupPokeListener

    @Autowired
    private lateinit var randomPickListener: GroupMessageRandomPickListener

    @Autowired
    private lateinit var groupInviteRequestListener: GroupInviteRequestListener

    @Autowired
    private lateinit var groupAdminChangeListener: GroupAdminChangeListener

    @Autowired
    private lateinit var groupMemberApproveListener: GroupMemberApproveListener

    @Autowired
    private lateinit var groupHonorListener: GroupHonorListener

    @Autowired
    private lateinit var groupMemberLeaveListener: GroupMemberLeaveListener

    @Autowired
    private lateinit var groupLuckyKingListener: GroupLuckyKingListener

    @Autowired
    private lateinit var picqBotXConfig: PicqBotXConfig

    override fun run(vararg args: String?) {
        /**
         * ??????????????????
         */
        val commands = arrayOf<IcqCommand>(
            rollCommand
        )

        /**
         * ?????????????????????
         */
        val listeners = arrayOf<IcqListener>(
            // ??????????????????
            heartBeatListener,
            // ????????????????????????
            localExceptionListener,
            // ????????????????????????
            friendMessageListener,
            // bot??????????????????
            friendAddListener,
            // bot?????????????????????
            friendPokeListener,
            // ????????????????????????
            friendMessageRecallListener,
            // ?????????????????????
            groupMessageHelpListener, //??????
            groupMessageCountListener, // ???????????????
            groupMessageCounterListener,
            groupMessageBotAtListener, // bot???at??????
            groupMessageBotRepeatListener, // bot??????
            groupMessageBilibiliParserListener, // bilibili??????
            groupMessageRecallListener, // ?????????????????????
            groupMessageRedPackListener, // ???????????????
            groupMessageControlRecallListener, // ??????bot?????????????????????
            groupMessageGuessMaimaiSongListener, // maimai???????????????
            groupMessageRandomMaimaiMusicListener, // maimai??????
            groupMessageMaimaiQueueCardListener, // maimai?????????????????????
            groupMessageArcadeQueueListener,    // ??????????????????
            groupMessageRandomMaimaiArcadeListener, // ??????maimai??????
            // bot????????????
            groupPokeListener,
            // ????????????
            randomPickListener,
            // bot????????????????????????
            groupInviteRequestListener,
            // ????????????????????????
            groupAdminChangeListener,
            // ??????????????????????????????(???bot?????????????????????)
            groupMemberApproveListener,
            // ?????????????????????(?????????..)
            groupHonorListener,
            // ??????????????????
            groupMemberLeaveListener,
            // ????????????????????????
            groupLuckyKingListener,
        )

        // ????????????????????? ( ???????????? )
        val bot = PicqBotX(
            PicqConfig(picqBotXConfig.port)
                .setDebug(false) // ????????????debug??????
                .setNoVerify(false) // ???????????????Q???????????? (?????????)
                .setCommandsAlsoCallEvents(true) // ??????????????????????????????
                .setUseAsyncCommands(true) // ????????????????????????
                .setMaintenanceMode(false) // ????????????????????????
                .setMaintenanceResponse("bot?????????") // ?????????????????? (???????????????????????????)
                .setAutoMultiAccountOptimizations(true) // ?????????????????????????????????????????????
                .setMultiAccountOptimizations(true) // ?????????????????????????????????????????????????????????
                .setEventPaused(false) // ??????????????????
                .setHttpPaused(false) // ????????????HTTP??????
                .setApiRateLimited(false) // ????????????????????????API (??????enable_rate_limited_actions=true
                .setApiAsync(false) // ??????????????????API
                .setCommandArgsSplitRegex(" ") // ????????????????????????????????????????????????
                .setSecret(picqBotXConfig.secret) // SHA1 ???????????? (???????????????????????????)
                .setAccessToken(picqBotXConfig.accessToken) // Access Token ???????????? (??????????????????????????????)
                .setColorSupportLevel(ColorSupportLevel.FORCED) // Logger?????????????????? (??????DISABLED??????????????????)
                .setLogPath("logs") // Logger???????????? (??????????????????????????????)
                .setLogFileName("PicqBotX-Log") // Logger???????????????
                .setLogInit(true) // ????????????????????????
        )

        // ??????????????????????????? ( ??????, ??????URL, ???????????? )
        try {
            bot.addAccount("Bot", picqBotXConfig.postHost, picqBotXConfig.postPort)
        } catch (e: Exception) {
            bot.logger.error("???????????????????????????", e)
            return
        }

        // ??????HyExp ( ????????? )
        bot.setUniversalHyExpSupport(true)

        // ????????????
        bot.config.isUseAsyncCommands = true

        // ?????????????????????, ???????????????????????????
        bot.eventManager.registerListeners(*listeners)

        // ?????????Debug??????????????????????????????????????????
        //if (!bot.getConfig().isDebug())
        //   bot.getEventManager().registerListener(new SimpleTextLoggingListener());

        // ?????????????????????
        // ??????????????????????????????, ????????????"!help"???????????????"!"
        bot.enableCommandManager("!", "/", "???", ".")

        // ????????????
        // ??? v3.0.1.730 ?????????????????????????????????, ?????????????????? (???4000ms), ???????????????????????????Bug
        bot.commandManager.registerCommands(*commands)

        // Debug??????????????????????????????
        bot.logger.debug(bot.commandManager.commands.toString())

        // ???????????????, ?????????????????????
        bot.startBot()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Starter::class.java, *args)
}
