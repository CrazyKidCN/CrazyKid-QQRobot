import cn.crazykid.qqrobot.entity.MaimaiMusic
import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.util.ObjectUtil
import cn.hutool.core.util.ReUtil
import cn.hutool.http.HttpRequest
import com.alibaba.fastjson.JSON
import com.google.common.collect.Lists
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.security.KeyManagementException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * 舞萌DX国服maimaiNET数据抓取脚本
 * 主要匹配html页面的class样式来获取内容, 如果国服更新splash, maimaiNET样式有调整的话, 此脚本GG
 *
 * @author CrazyKid
 * @date 2021/3/25 15:10
 */
object MaimaiNetCNDataCatcherTest {
    /**
     * 这里存放成功抓取的数据
     */
    private val maidata: MutableList<MaimaiMusic> = Lists.newArrayList()

    /**
     * 存放歌曲标题计数器
     */
    private val titleCounter: MutableMap<String, Int> = mutableMapOf()

    /**
     * 这里存放下一次请求maimaiNET时携带的cookie
     */
    private var cookie = mutableMapOf<String, String>()

    /**
     * 这里标记抓取是否已完成
     */
    private var finished = false

    @JvmStatic
    fun main(args: Array<String>) {
        // 读取之前已抓取的数据
        val str = ResourceUtil.readUtf8Str("maidata.json")
        maidata.addAll(JSON.parseArray(str, MaimaiMusic::class.java))

        // 版本更新的时候, 手动把image_file字段全删掉，以便脚本重新抓取
        //maidata.forEach { it.imageFile = null }

        var m = maidata.filter { it.imageFile != null }
        val json = JSON.toJSONString(m)
        println(json)   // 建议这里打断点 然后直接debugger里复制json的文本比较方便

        // 在这里填写初始 Cookie
        cookie["_t"] = ""
        cookie["userId"] = ""

        /**
        0: maimai
        1: maimai PLUS
        2: GreeN
        3: GreeN PLUS
        4: ORANGE
        5: ORANGE PLUS
        6: PiNK
        7: PiNK PLUS
        8: MURASAKi
        9: MURASAKi PLUS
        10: MiLK
        11: MiLK PLUS
        12: FiNALE
        13: 舞萌DX
        15: 舞萌DX 2021
        17: 舞萌DX 2022
        19: 舞萌DX 2023
         */

        // 因为有时候获取歌曲详情会失败, 所以一直遍历直到全部歌曲详情都获取成功为止.

        try {
            for (version in 0..19) {
                if (version == 14 || version == 16 || version == 18) {
                    continue
                }
                finished = false
                while (!finished) {
                    finished = true
                    this.getBasicInfo(version)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            val json = JSON.toJSONString(maidata)
            println(json)   // 建议这里打断点 然后直接debugger里复制json的文本比较方便
            println(cookie) // 这里打印的cookie可用作下次运行脚本时使用, 不用自己再重新抓包

            val titleCounterMoreThanOne = titleCounter.filterValues { it > 1 }
            println(titleCounterMoreThanOne)

            val deletedMaidata: MutableList<MaimaiMusic> = Lists.newArrayList()
            // 下载封面
            for (i in maidata.indices) {
                // 判断imageFile如果为null代表该歌曲被删除了.
                if (maidata[i].imageFile == null) {
                    deletedMaidata.add(maidata[i])
                    println("歌曲: ${maidata[i].title} 已被删除...")
                    continue
                }
                print("[${i + 1}/${maidata.size}] 开始下载歌曲 ${maidata[i].title} 的封面.. ")
                // 在path填写封面下载到本地路径
                this.downloadCoverToLocal(
                    "/Users/crazykid/git/maimaiDX-CN-songs-database/cover/",
                    maidata[i].imageFile!!
                )
            }

            maidata.removeAll(deletedMaidata)
            println(json)   // 建议这里打断点 然后直接debugger里复制json的文本比较方便
        }
    }

    /**
     * 获取歌曲基本信息
     */
    private fun getBasicInfo(version: Int) {
        //val document = this.queryUrl("https://maimai.wahlap.com/maimai-mobile/record/musicGenre/search/?genre=99&diff=0")
        val document =
            this.queryUrl("https://maimai.wahlap.com/maimai-mobile/record/musicVersion/search/?version=$version&diff=0")

        // 歌曲盒子div
        //val songBox = document.getElementsByClass("w_450 m_15 p_r f_0")
        val songBox = document.getElementsByClass("music_basic_score_back")

        var currentMaimaiMusic: MaimaiMusic?

        for (element in songBox) {
            val title = element.getElementsByClass("music_name_block t_l f_13 break")
            val type = this.getStandardOrDx(element)
            /*val type = when (version) {
                13 -> "DX"
                15 -> "DX"
                else -> "SD"
            }*/

            // 统计标题出现的次数, 以判断歌曲重名的情况
            titleCounter.compute(title.text() + type) { k, v -> if (v == null) 1 else v + 1 }
            // 获取当前歌曲标题出现的次数
            val titleCount = titleCounter[title.text() + type]

            var filterMaidata = maidata.filter { music -> music.title == title.text() }
            if (filterMaidata.isNotEmpty()) {
                currentMaimaiMusic = filterMaidata[0]
                if (ObjectUtil.isEmpty(currentMaimaiMusic.imageFile)) {
                    // 一旦发现歌曲详情有为空的, 即标记未完成
                    finished = false
                } else if (type == "SD" && ObjectUtil.isNotEmpty(currentMaimaiMusic.levMas)) {
                    continue
                } else if (type == "DX" && ObjectUtil.isNotEmpty(currentMaimaiMusic.dxLevMas)) {
                    continue
                }
            } else {
                // 方便debug
                /*if (maidata.size > 10) {
                    return
                }*/
                currentMaimaiMusic = MaimaiMusic()
            }


            val idx = element.getElementsByTag("input")[0].`val`()

            val detailSuccessCount = maidata.count { ObjectUtil.isNotEmpty(it.imageFile) }
            println("歌曲总数/歌曲详情获取成功数: ${maidata.size}/$detailSuccessCount")
            //println("类型: $type")
            print("标题: ${title.text()}")
            //println("等级: ${level.text()}")
            //println("分类: $currentCategory")

            currentMaimaiMusic.title = title.text()
            currentMaimaiMusic.version = when (version) {
                0 -> "maimai"
                1 -> "maimai PLUS"
                2 -> "GreeN"
                3 -> "GreeN PLUS"
                4 -> "ORANGE"
                5 -> "ORANGE PLUS"
                6 -> "PiNK"
                7 -> "PiNK PLUS"
                8 -> "MURASAKi"
                9 -> "MURASAKi PLUS"
                10 -> "MiLK"
                11 -> "MiLK PLUS"
                12 -> "FiNALE"
                13 -> "舞萌DX"
                14 -> "其它"
                15 -> "舞萌DX 2021"
                17 -> "舞萌DX 2022"
                19 -> "舞萌DX 2023"
                else -> "未知"
            }

            try {
                currentMaimaiMusic = this.getSongDetail(idx, currentMaimaiMusic, type!!)
                if (ObjectUtil.isNotEmpty(currentMaimaiMusic.imageFile)) {
                    println("[获取歌曲详情成功!]")
                } else {
                    // 一旦歌曲详情获取失败 即标记未完成
                    finished = false
                }
            } catch (e: ConnectException) {
                println("获取歌曲详情发生异常，跳过！")
                continue
            }

            // 第二次过滤 以图片名称为依据，解决歌曲重名情况
            filterMaidata = maidata.filter { music -> music.imageFile == currentMaimaiMusic.imageFile }
            if (filterMaidata.isEmpty()) {
                maidata.add(currentMaimaiMusic)
            }
        }
    }

    /**
     * 获取该元素是dx还是标谱
     */
    private fun getStandardOrDx(element: Element): String? {
        val eleDx = element.getElementsByClass("music_kind_icon_dx")
        val eleSd = element.getElementsByClass("music_kind_icon_standard")
        val ele = element.getElementsByClass("music_kind_icon")

        if (!eleDx.isEmpty() && eleDx.any { e -> e.className().contains("_btn_on", true) }) {
            return "DX"
        } else if (!eleSd.isEmpty() && eleSd.any { e -> e.className().contains("_btn_on", true) }) {
            return "SD"
        } else {
            for (e in ele) {
                val src = e.attr("src")
                return when {
                    src.contains("music_dx", true) -> {
                        "DX"
                    }

                    src.contains("music_standard", true) -> {
                        "SD"
                    }

                    else -> {
                        null
                    }
                }
            }
        }
        return null
    }

    /**
     * 获取歌曲详情
     */
    private fun getSongDetail(idx: String, music: MaimaiMusic, type: String): MaimaiMusic {
        val document = queryUrl("https://maimai.wahlap.com/maimai-mobile/record/musicDetail/?idx=$idx")

        val artist = document.getElementsByClass("m_5 f_12 break")
        val coverElements = document.getElementsByClass("w_180 m_5 f_l")

        var cover: String?
        if (coverElements.isNotEmpty()) {
            cover = coverElements[0].attr("src")
            cover = cover.substring(cover.lastIndexOf("/") + 1)
        } else {
            return music
        }

        val difficult = document.getElementsByClass("music_lv_back m_3 t_c f_14")
        val category = document.getElementsByClass("m_10 m_t_5 t_r f_12 blue").text()

        music.artist = artist.text()
        music.imageFile = cover
        music.category = category

        for (index in difficult.indices) {
            if (type == "SD") {
                when (index) {
                    0 -> music.levBas = difficult[index].text()
                    1 -> music.levAdv = difficult[index].text()
                    2 -> music.levExp = difficult[index].text()
                    3 -> music.levMas = difficult[index].text()
                    4 -> music.levReMas = difficult[index].text()
                }
            } else if (type == "DX") {
                when (index) {
                    0 -> music.dxLevBas = difficult[index].text()
                    1 -> music.dxLevAdv = difficult[index].text()
                    2 -> music.dxLevExp = difficult[index].text()
                    3 -> music.dxLevMas = difficult[index].text()
                    4 -> music.dxLevReMas = difficult[index].text()
                }
            }
        }
        return music
    }

    @Throws(ConnectException::class)
    fun queryUrl(url: String): Document {
        //println("Querying: $url")
        val con = Jsoup.connect(url)
        con.cookies(cookie)
        con.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        con.header(
            "User-Agent",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_16) AppleWebKit/605.1.15 (KHTML, like Gecko) MicroMessenger/6.8.0(0x16080000) MacWechat/3.0(0x13000005) NetType/WIFI WindowsWechat"
        )
        con.sslSocketFactory(socketFactory())

        val resp: Connection.Response
        try {
            resp = con.method(Connection.Method.GET).execute()
        } catch (e: ConnectException) {
            e.printStackTrace()
            throw e
        }

        // 保存刷新后的cookie以便下次调用
        val cookies = resp.multiHeaders()["Set-Cookie"]
        if (cookies!!.size < 1) {
            //throw RuntimeException("刷新cookie失败! 检查初始cookie是否正确!")
            println("刷新cookie失败! 5s后递归尝试...")
            Thread.sleep(5000)
            return this.queryUrl(url)
        } else {
            // 给个睡眠防止触发并发限制
            Thread.sleep(1000)
        }
        cookies.forEach { c ->
            ReUtil.getGroup1("_t=(.*);", c)?.let { t ->
                cookie["_t"] = t
            } ?: ReUtil.getGroup1("userId=(.*);", c)?.let { u ->
                cookie["userId"] = u
            }
        }

        return Jsoup.parse(resp.body())
    }

    private fun downloadCoverToLocal(path: String, imageFile: String) {
        // 储存目录如果不存在，创建之
        if (!FileUtil.exist(path)) {
            FileUtil.mkdir(path)
        }
        // 储存目标文件
        val targetFile = File(path + imageFile)

        // 如果已经存在, 跳过
        if (FileUtil.exist(targetFile)) {
            println("[封面文件已存在, 跳过...]")
            return
        }

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = HttpRequest.get("https://maimai.wahlap.com/maimai-mobile/img/Music/$imageFile")
                .setSSLProtocol("TLSv1.2")
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_16) AppleWebKit/605.1.15 (KHTML, like Gecko) MicroMessenger/6.8.0(0x16080000) MacWechat/3.0(0x13000005) NetType/WIFI WindowsWechat"
                )
                .execute()
                .bodyStream()
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            outputStream = FileOutputStream(targetFile)
            outputStream.write(buffer)
            println("[成功!]")
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    /**
     * 解决请求https地址报证书异常问题
     */
    private fun socketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })

        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            return sslContext.socketFactory
        } catch (e: Exception) {
            when (e) {
                is RuntimeException, is KeyManagementException -> {
                    throw RuntimeException("Failed to create a SSL socket factory", e)
                }

                else -> throw e
            }
        }
    }
}
