package cn.crazykid.qqrobot.service.impl

import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentImage
import cn.crazykid.qqrobot.entity.DivingFishMaimaiMusicData
import cn.crazykid.qqrobot.entity.MaimaiMusic
import cn.crazykid.qqrobot.service.IMaimaiMusicDataService
import cn.crazykid.qqrobot.util.PathUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.text.csv.CsvUtil
import cn.hutool.core.thread.ThreadUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.Lists
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

        val MAI_DATA: MutableList<MaimaiMusic> = Lists.newArrayList()
    }

    @Value("\${maimaiMusicDataApi.divingFish:}")
    private var divingFishApi: String? = null

    @Value("\${maimaiMusicDataApi.xrayAlias:}")
    private var xrayAliasApi: String? = null


    @PostConstruct
    fun init() {
        // 读取水鱼查分器歌曲数据
        val divingFishMaimaiMusicDataPath = PathUtil.getClassPath() + "data/" + DIVING_FISH_MAIMAI_MUSIC_DATA_FILE_NAME;
        val updateDivingFishMaimaiMusicDataTask: Runnable = Runnable {
            try {
                // 拉取数据存储本地
                HttpUtil.downloadFileFromUrl(divingFishApi, divingFishMaimaiMusicDataPath)
                println("divingFishMaimaiMusicData Update Success")
            } catch (ignored: Exception) {
                // ignored
            }
        }
        if (FileUtil.exist(divingFishMaimaiMusicDataPath)) {
            // 如果此前已拉取过, 则异步执行更新, 避免初始化卡进程
            ThreadUtil.execute(updateDivingFishMaimaiMusicDataTask);
        } else {
            // 没拉取过则同步先拉取
            updateDivingFishMaimaiMusicDataTask.run();
        }
        val dfjsonStr = FileUtil.readUtf8String(divingFishMaimaiMusicDataPath)
        DIVING_FISH_MAIMAI_MUSIC_DATA = JSON.parseArray(dfjsonStr, DivingFishMaimaiMusicData::class.java)

        // 读取 xray bot maimai 歌曲别名数据
        val xrayMaimaiMusicAliasPath = PathUtil.getClassPath() + "data/" + XRAY_MAIMAI_MUSIC_ALIAS_FILE_NAME;
        val updateXrayMaimaiMusicAliasTask: Runnable = Runnable {
            try {
                // 拉取数据存储本地
                HttpUtil.downloadFileFromUrl(xrayAliasApi, xrayMaimaiMusicAliasPath)
                println("xrayMaimaiMusicAlias Update Success")
            } catch (ignored: Exception) {
                // ignored
            }
        }
        if (FileUtil.exist(xrayMaimaiMusicAliasPath)) {
            // 如果此前已拉取过, 则异步执行更新, 避免初始化卡进程
            ThreadUtil.execute(updateXrayMaimaiMusicAliasTask);
        } else {
            // 没拉取过则同步先拉取
            updateXrayMaimaiMusicAliasTask.run();
        }
        val xrayjsonStr = FileUtil.readUtf8String(xrayMaimaiMusicAliasPath)
        XRAY_MAIMAI_MUSIC_ALIAS = JSON.parseObject(xrayjsonStr)


        // 读取本地 aliases.csv 别名文件
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

        // 本地歌曲别名数据与 xray 的别名数据合并
        XRAY_MAIMAI_MUSIC_ALIAS?.run {
            forEach { aliasName, musicIdList ->
                val list = musicIdList as JSONArray
                for (musicIdObj in list) {
                    val musicIdStr = musicIdObj.toString()
                    var musicId: Long
                    try {
                        musicId = musicIdStr.toLong()
                    } catch (e: Exception) {
                        // xray 的别名 json 里可能出现 id 为"未找到"的数据, 转化类型会报错
                        continue
                    }
                    val musicData = DIVING_FISH_MAIMAI_MUSIC_DATA?.filter {
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

        val str = ResourceUtil.readUtf8Str("maidata.json")
        MAI_DATA.addAll(JSON.parseArray(str, MaimaiMusic::class.java))
    }

    override fun getDivingFishMusicDataByTitle(title: String): DivingFishMaimaiMusicData? {
        val list = DIVING_FISH_MAIMAI_MUSIC_DATA!!.filter { it.title.equals(title) }
        return if (list.isEmpty()) null else list[0]
    }

    override fun getAliasByMusicTitle(title: String): List<String>? {
        return SONG_ALIASES[title]
    }

    override fun searchSong(keyword: String): String {
        val divingFishMusicList: MutableList<DivingFishMaimaiMusicData> = mutableListOf()
        // try as song id
        try {
            val songId = keyword.toLong()
            var list = DIVING_FISH_MAIMAI_MUSIC_DATA!!.filter { it.id!! == songId }
            if (list.isNotEmpty()) {
                divingFishMusicList.addAll(list)
                //return this.getSongInfoStr(list[0].id!!)
            }
        } catch (ignore: Exception) {
        }
        // try as song alias
        SONG_ALIASES.forEach { (title, aliasList) ->
            if (aliasList.contains(keyword)) {
                val divingfishMusic = this.getDivingFishMusicDataByTitle(title)
                if (divingfishMusic != null) {
                    divingFishMusicList.add(divingfishMusic)
                }
            }
        }
        // if still no result, try as song title
        if (divingFishMusicList.isEmpty()) {
            val filterTitleMusicList = DIVING_FISH_MAIMAI_MUSIC_DATA!!.filter { it.title!!.contains(keyword, true) }
            divingFishMusicList.addAll(filterTitleMusicList)
        }

        if (divingFishMusicList.size > 10) {
            return "查询结果过多(${divingFishMusicList.size}条), 请输入更精确的关键字缩小查询范围"
        } else if (divingFishMusicList.size > 1) {
            val m = MessageBuilder().add("您要找的是不是")
            divingFishMusicList.forEach {
                m.newLine()
                    .add(it.id).add(": ").add(it.title)
            }
            m.newLine()
                .add("输入\"id+编号\"查看详情(例如id${divingFishMusicList[0].id}查看第一首)")
            return m.toString()
        } else if (divingFishMusicList.size == 1) {
            return this.getSongInfoStr(divingFishMusicList[0].id!!)
        }
        return "未找到歌曲信息"
    }

    override fun getSongInfoStr(id: Long): String {
        var list = DIVING_FISH_MAIMAI_MUSIC_DATA!!.filter { it.id!! == id }
        if (list.isEmpty()) {
            return "未找到歌曲信息"
        }
        val title = list[0].title
        var music: MaimaiMusic? = null
        var divingFishMusicSD: DivingFishMaimaiMusicData? = null
        var divingFishMusicDX: DivingFishMaimaiMusicData? = null

        music = MaimaiMusic()
        music.title = title
        music.artist = list[0].basic_info!!.artist
        music.category = list[0].basic_info!!.genre
        music.version = list[0].basic_info!!.from

        if ("SD" == list[0].type) {
            divingFishMusicSD = list[0]
        } else if ("DX" == list[0].type) {
            divingFishMusicDX = list[0]
        }


        if (divingFishMusicSD == null) {
            list = DIVING_FISH_MAIMAI_MUSIC_DATA!!.filter { it.title!! == title && it.type == "SD" }
            if (list.isNotEmpty()) {
                divingFishMusicSD = list[0]
            }
        }
        if (divingFishMusicDX == null) {
            list = DIVING_FISH_MAIMAI_MUSIC_DATA!!.filter { it.title!! == title && it.type == "DX" }
            if (list.isNotEmpty()) {
                divingFishMusicDX = list[0]
            }
        }

        val filterMusicList = MAI_DATA.filter { data -> data.title == title }
        if (filterMusicList.isNotEmpty()) {
            music = filterMusicList.first()
        }

        val m = MessageBuilder()
        m.add(ComponentImage("https://maimai.wahlap.com/maimai-mobile/img/Music/" + music.imageFile)).newLine()
            .add("标题: ${music.title}").newLine()
            .add("作曲: ${music.artist}").newLine()
            .add("分类: ${music.category}").newLine()
        if (divingFishMusicSD != null) {
            m.add("BPM: ").add(divingFishMusicSD.basic_info!!.bpm).newLine()
        } else if (divingFishMusicDX != null) {
            m.add("BPM: ").add(divingFishMusicDX.basic_info!!.bpm).newLine()
        }
        m.add("版本: ${music.version}").newLine()

        if (divingFishMusicSD != null) {
            m.newLine().add("标谱水鱼id: ").add(divingFishMusicSD.id)
        }
        if (music.levMas != null) {
            m.newLine()
                .add("标谱等级[${music.levBas}/${music.levAdv}/${music.levExp}/${music.levMas}")
            if (music.levReMas != null) {
                m.add("/${music.levReMas}")
            }
            m.add("]")
        }
        if (divingFishMusicSD != null) {
            m.newLine().add("标谱定数[")
            divingFishMusicSD.ds!!.forEachIndexed { index, dingshu ->
                m.add(dingshu)
                if (index < divingFishMusicSD.ds!!.size - 1) {
                    m.add("/")
                } else {
                    m.add("]")
                }
            }
        }

        if (divingFishMusicDX != null) {
            m.newLine().add("DX谱水鱼id: ").add(divingFishMusicDX.id)
        }
        if (music.dxLevMas != null) {
            m.newLine()
                .add("DX谱等级[${music.dxLevBas}/${music.dxLevAdv}/${music.dxLevExp}/${music.dxLevMas}")
            if (music.dxLevReMas != null) {
                m.add("/${music.dxLevReMas}")
            }
            m.add("]")
        }
        if (divingFishMusicDX != null) {
            m.newLine().add("DX谱定数[")
            divingFishMusicDX.ds!!.forEachIndexed { index, dingshu ->
                m.add(dingshu)
                if (index < divingFishMusicDX.ds!!.size - 1) {
                    m.add("/")
                } else {
                    m.add("]")
                }
            }
        }
        return m.toString()
    }
}
