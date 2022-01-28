package cn.crazykid.qqrobot.exception

/**
 * @author CrazyKid
 * @date 2022/1/21 13:40
 */
class OperateFailedException(val sendMessage: Boolean, override val message: String) : Throwable() {

}
