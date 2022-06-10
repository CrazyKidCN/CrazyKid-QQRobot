package cn.crazykid.qqrobot.enums

/**
 * 酷Q HTTP 请求 message_type 枚举
 * @author CrazyKid
 * @since 2020/12/27
 */
enum class MessageTypeEnum(val code: String) {
    // 群聊
    GROUP("group"),

    // 私聊
    PRIVATE("private")
}
