package cn.crazykid.qqrobot.service.impl

import cn.crazykid.qqrobot.entity.DivingFishMaimaiMusicData
import cn.crazykid.qqrobot.service.IMaimaiMusicDataService
import cn.crazykid.qqrobot.util.PathUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.text.csv.CsvUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

/**
 * @author CrazyKid
 * @date 2022/7/18 11:26
 */
@Service
class MaimaiMusicDataServiceImpl : IMaimaiMusicDataService {

    companion object {
        const val DIVING_FISH_MAIMAI_MUSIC_DATA_FILE_NAME = "divingFishMaimaiMusicData.json"
        const val XRAY_MAIMAI_MUSIC_ALIAS_FILE_NAME = "xrayMaimaiMusicAlias.json"
        var DIVING_FISH_MAIMAI_MUSIC_DATA: MutableList<DivingFishMaimaiMusicData>? = null
        var XRAY_MAIMAI_MUSIC_ALIAS: JSONObject? = null

        val SONG_ALIASES: MutableMap<String, MutableList<String>> = mutableMapOf()
    }

    @Value("\${maimaiMusicDataApi.divingFish:}")
    private var divingFishApi: String? = null

    @Value("\${maimaiMusicDataApi.xrayAlias:}")
    private var xrayAliasApi: String? = null


    @PostConstruct
    fun init() {
        // read diving fish maimai music data
        val divingFishMaimaiMusicDataPath = PathUtil.getClassPath() + "data/" + DIVING_FISH_MAIMAI_MUSIC_DATA_FILE_NAME;
        try {
            HttpUtil.downloadFileFromUrl(divingFishApi, divingFishMaimaiMusicDataPath)
            println("divingFishMaimaiMusicData Update Success")
        } catch (ignored: Exception) {
            // ignored
        }
        val dfjsonStr = FileUtil.readUtf8String(divingFishMaimaiMusicDataPath)
        DIVING_FISH_MAIMAI_MUSIC_DATA = JSON.parseArray(dfjsonStr, DivingFishMaimaiMusicData::class.java)

        // read xray maimai music alias
        val xrayMaimaiMusicAliasPath = PathUtil.getClassPath() + "data/" + XRAY_MAIMAI_MUSIC_ALIAS_FILE_NAME;
        try {
            HttpUtil.downloadFileFromUrl(xrayAliasApi, xrayMaimaiMusicAliasPath)
            println("xrayMaimaiMusicAlias Update Success")
        } catch (ignored: Exception) {
            // ignored
        }
        val xrayjsonStr = FileUtil.readUtf8String(xrayMaimaiMusicAliasPath)
        XRAY_MAIMAI_MUSIC_ALIAS = JSON.parseObject(xrayjsonStr)


        // read local aliases.csv file
        val reader = CsvUtil.getReader()
        val data = reader.readFromStr(ResourceUtil.readUtf8Str("aliases.csv"))
        val rows = data.rows
        for (csvRow in rows) {
            for (str in csvRow.rawList) {
                val split = StrUtil.splitTrim(str, "\t")
                if (split.size > 1) {
                    split.forEachIndexed { index, songName ->
                        if (index > 0) {
                            SONG_ALIASES.merge(split[0], mutableListOf(songName)) { existed, replacement ->
                                run {
                                    existed.addAll(replacement)
                                    existed
                                }
                            }
                        }
                    }
                }
            }
        }

        // merge xray's music alias into the map
        XRAY_MAIMAI_MUSIC_ALIAS?.run {
            forEach { aliasName, musicIdList ->
                val list = musicIdList as JSONArray
                for (musicIdObj in list) {
                    val musicId = musicIdObj.toString().toLong()
                    val musicData = (DIVING_FISH_MAIMAI_MUSIC_DATA as MutableList<DivingFishMaimaiMusicData>?)?.filter {
                        it.id == musicId
                    }
                    if (!musicData.isNullOrEmpty()) {
                        val songAlias = SONG_ALIASES[musicData[0].title!!]
                        if (!songAlias.isNullOrEmpty() && songAlias.none { aliasName.equals(it) }) {
                            songAlias.add(aliasName)
                        }
                    }
                }
            }
        }
    }

    override fun getDivingFishMusicDataByTitle(title: String): DivingFishMaimaiMusicData? {
        val list = DIVING_FISH_MAIMAI_MUSIC_DATA!!.filter { it.title.equals(title) }
        return if (list.isEmpty()) null else list[0]
    }

    override fun getAliasByMusicTitle(title: String): List<String>? {
        return SONG_ALIASES[title]
    }
}
