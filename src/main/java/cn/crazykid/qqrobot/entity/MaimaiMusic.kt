package cn.crazykid.qqrobot.entity

import com.alibaba.fastjson.annotation.JSONField

/**
 * maimai歌曲对象类
 *
 * @author CrazyKid
 * @since 2021年03月25日11:06:34
 */
class MaimaiMusic {
    @JSONField(name = "title", ordinal = 1)
    var title: String? = null

    @JSONField(name = "artist", ordinal = 2)
    var artist: String? = null

    @JSONField(name = "category", ordinal = 3)
    var category: String? = null

    @JSONField(name = "image_file", ordinal = 4)
    var imageFile: String? = null

    @JSONField(name = "lev_bas", ordinal = 5)
    var levBas: String? = null

    @JSONField(name = "lev_adv", ordinal = 6)
    var levAdv: String? = null

    @JSONField(name = "lev_exp", ordinal = 7)
    var levExp: String? = null

    @JSONField(name = "lev_mas", ordinal = 8)
    var levMas: String? = null

    @JSONField(name = "lev_remas", ordinal = 9)
    var levReMas: String? = null

    @JSONField(name = "dx_lev_bas", ordinal = 10)
    var dxLevBas: String? = null

    @JSONField(name = "dx_lev_adv", ordinal = 11)
    var dxLevAdv: String? = null

    @JSONField(name = "dx_lev_exp", ordinal = 12)
    var dxLevExp: String? = null

    @JSONField(name = "dx_lev_mas", ordinal = 13)
    var dxLevMas: String? = null

    @JSONField(name = "dx_lev_remas", ordinal = 14)
    var dxLevReMas: String? = null

    @JSONField(name = "version", ordinal = 15)
    var version: String? = null

    fun containsLevel(level: String): Boolean {
        return levBas == level ||
                levAdv == level ||
                levExp == level ||
                levMas == level ||
                levReMas == level ||
                dxLevBas == level ||
                dxLevAdv == level ||
                dxLevExp == level ||
                dxLevMas == level ||
                dxLevReMas == level
    }

    fun containsType(type: String): Boolean {
        if (type.equals("SD", true) || type == "标准") {
            if (levMas != null) {
                return true
            }
        } else if (type.equals("DX", true) && dxLevMas != null) {
            return true
        }
        return false
    }

    fun containsCategory(category: String): Boolean {
        return when {
            category.equals("N站", true) -> {
                this.category!!.contains("niconico")
            }
            category.equals("V家", true) -> {
                this.category!!.contains("VOCALOID")
            }
            category.equals("原创", true) -> {
                this.category!!.contains("舞萌")
            }
            category.equals("新曲", true) -> {
                this.version!!.contains("舞萌DX 2021")
            }
            else -> {
                this.category!!.contains(category)
            }
        }
    }
}
