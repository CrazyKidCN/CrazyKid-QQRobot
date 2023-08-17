package cn.crazykid.qqrobot.service

import cn.crazykid.qqrobot.entity.DivingFishMaimaiMusicData

/**
 * @author CrazyKid
 * @date 2022/7/18 11:25
 */
interface IMaimaiMusicDataService {
    /**
     * 根据歌曲标题获取水鱼查分器的歌曲数据
     */
    fun getDivingFishMusicDataByTitle(title: String): DivingFishMaimaiMusicData?

    /**
     * 根据歌曲标题获取歌曲别名列表
     */
    fun getAliasByMusicTitle(title: String): List<String>?

    /**
     * 关键字查歌
     */
    fun searchSong(keyword: String): String

    /**
     * 根据水鱼id获取歌曲信息文本
     */
    fun getSongInfoStr(id: Long): String
}
