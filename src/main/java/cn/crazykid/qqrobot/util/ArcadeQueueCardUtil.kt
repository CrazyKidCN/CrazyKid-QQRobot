package cn.crazykid.qqrobot.util

import cn.crazykid.qqrobot.entity.Arcade
import com.alibaba.fastjson.JSON
import java.util.*

/**
 * @author CrazyKid
 * @date 2022/1/9 10:41
 */
object ArcadeQueueCardUtil {
    fun addCard(arcade: Arcade, num: Int, updateBy: String) {
        arcade.cardNum = arcade.cardNum + num
        arcade.cardUpdateBy = updateBy
        arcade.cardUpdateTime = Date()
    }

    fun setCard(arcade: Arcade, num: Int, updateBy: String) {
        arcade.cardNum = num
        arcade.cardUpdateBy = updateBy
        arcade.cardUpdateTime = Date()
    }

    fun setClose(arcade: Arcade, close: Int) {
        arcade.close = close
    }

    fun getArcadeAlias(arcade: Arcade): List<String> {
        return JSON.parseArray(arcade.aliasJson, String::class.java)
    }

    fun getArcadeGroupNames(arcade: Arcade): List<String> {
        try {
            return JSON.parseArray(arcade.groupName, String::class.java)
        } catch (e: Exception) {
            return Collections.emptyList()
        }
    }

    fun getArcadeGroupNumber(arcade: Arcade): List<Long> {
        return JSON.parseArray(arcade.groupNumberJson, Long::class.java)
    }
}
