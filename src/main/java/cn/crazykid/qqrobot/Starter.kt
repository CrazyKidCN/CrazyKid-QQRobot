package cn.crazykid.qqrobot

import cc.moecraft.icq.PicqBotX
import cc.moecraft.icq.PicqConfig
import cc.moecraft.icq.command.interfaces.IcqCommand
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.logger.environments.ColorSupportLevel
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
         * 要注册的指令
         */
        val commands = arrayOf<IcqCommand>()

        /**
         * 要注册的监听器
         */
        val listeners = arrayOf<IcqListener>(
            // 心跳事件监听
            heartBeatListener,
            // 本地异常事件监听
            localExceptionListener,
            // 好友私聊事件监听
            friendMessageListener,
            // bot被加好友事件
            friendAddListener,
            // bot被好友被戳事件
            friendPokeListener,
            // 好友撤回消息事件
            friendMessageRecallListener,
            // 群消息事件监听
            groupMessageCountListener, // 统计消息数
            groupMessageCounterListener,
            groupMessageBotAtListener, // bot被at事件
            groupMessageBotRepeatListener, // bot复读
            groupMessageBilibiliParserListener, // bilibili解析
            groupMessageRecallListener, // 群消息撤回事件
            groupMessageRedPackListener, // 群红包监听
            groupMessageControlRecallListener, // 控制bot撤回自己的消息
            groupMessageGuessMaimaiSongListener, // maimai猜歌小游戏
            groupMessageRandomMaimaiMusicListener, // maimai随歌
            groupMessageMaimaiQueueCardListener, // maimai排卡数计数功能
            groupMessageArcadeQueueListener,    // 机厅排队助手
            groupMessageRandomMaimaiArcadeListener, // 随个maimai机厅
            // bot被戳事件
            groupPokeListener,
            // 随机选择
            randomPickListener,
            // bot被邀请加入群事件
            groupInviteRequestListener,
            // 群管理员变更事件
            groupAdminChangeListener,
            // 群成员被同意进群事件(含bot自己被同意进群)
            groupMemberApproveListener,
            // 群荣耀变更事件(龙王等..)
            groupHonorListener,
            // 群友退群事件
            groupMemberLeaveListener,
            // 群红包运气王事件
            groupLuckyKingListener,
        )

        // 创建机器人对象 ( 传入配置 )
        val bot = PicqBotX(
            PicqConfig(picqBotXConfig.port)
                .setDebug(false) // 是否输出debug信息
                .setNoVerify(false) // 是否跳过酷Q版本验证 (不推荐)
                .setCommandsAlsoCallEvents(true) // 指令是否触发消息事件
                .setUseAsyncCommands(true) // 是否异步执行指令
                .setMaintenanceMode(false) // 是否启用维护模式
                .setMaintenanceResponse("bot维护中") // 维护模式回复 (设为空就不会回复了)
                .setAutoMultiAccountOptimizations(true) // 是否自动判断是否开启多账号优化
                .setMultiAccountOptimizations(true) // 不自动判断的时候是否手动开启多账号优化
                .setEventPaused(false) // 是否暂停事件
                .setHttpPaused(false) // 是否暂停HTTP接收
                .setApiRateLimited(false) // 是否启用限速调用API (需要enable_rate_limited_actions=true
                .setApiAsync(false) // 是否异步调用API
                .setCommandArgsSplitRegex(" ") // 解析指令的时候用来分割参数的正则
                .setSecret(picqBotXConfig.secret) // SHA1 验证秘钥 (设置为空就是不验证)
                .setAccessToken(picqBotXConfig.accessToken) // Access Token 访问令牌 (设置为空就是不用令牌)
                .setColorSupportLevel(ColorSupportLevel.FORCED) // Logger颜色支持级别 (设为DISABLED就没有颜色了)
                .setLogPath("logs") // Logger日志路径 (设为空就不输出文件了)
                .setLogFileName("PicqBotX-Log") // Logger日志文件名
                .setLogInit(true) // 是否输出启动日志
        )

        // 添加一个机器人账户 ( 名字, 发送URL, 发送端口 )
        try {
            bot.addAccount("Bot", picqBotXConfig.postHost, picqBotXConfig.postPort)
        } catch (e: Exception) {
            bot.logger.error("添加机器人账户失败", e)
            return
        }

        // 启用HyExp ( 非必要 )
        bot.setUniversalHyExpSupport(true)

        // 设置异步
        bot.config.isUseAsyncCommands = true

        // 注册事件监听器, 可以注册多个监听器
        bot.eventManager.registerListeners(*listeners)

        // 在没有Debug的时候加上这个消息日志监听器
        //if (!bot.getConfig().isDebug())
        //   bot.getEventManager().registerListener(new SimpleTextLoggingListener());

        // 启用指令管理器
        // 这些字符串是指令前缀, 比如指令"!help"的前缀就是"!"
        bot.enableCommandManager("!", "/", "！", ".")

        // 注册指令
        // 从 v3.0.1.730 之后不会自动注册指令了, 因为效率太低 (≈4000ms), 而且在其他框架上有Bug
        //bot.commandManager.registerCommands(*commands)

        // Debug输出所有已注册的指令
        bot.logger.debug(bot.commandManager.commands.toString())

        // 启动机器人, 不会占用主线程
        bot.startBot()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Starter::class.java, *args)
}
