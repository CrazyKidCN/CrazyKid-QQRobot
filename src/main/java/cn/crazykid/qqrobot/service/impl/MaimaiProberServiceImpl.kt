package cn.crazykid.qqrobot.service.impl

import cn.crazykid.qqrobot.entity.DivingFishMaimaiPlayerData
import cn.crazykid.qqrobot.service.IMaimaiProberService
import cn.hutool.http.HttpRequest
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import org.springframework.stereotype.Service

/**
 * @author CrazyKid
 * @date 2023/7/27 10:29
 */
@Service
class MaimaiProberServiceImpl : IMaimaiProberService {

//    @Autowired
//    private lateinit var maimaiProberImageDrawer: MaimaiProberImageDrawer

    companion object {
        const val HOST = "https://www.diving-fish.com"
        const val QUERY_PLAYER_API = "/api/maimaidxprober/query/player"
        const val QUERY_PLATE_API = "/api/maimaidxprober/query/plate"
    }

    override fun queryPlayerData(type: String, value: Any): DivingFishMaimaiPlayerData? {
        val params = JSONObject()
            .set(type, value)
            .set("b50", true)

        val httpResponse = HttpRequest.post("${HOST}${QUERY_PLAYER_API}")
            .body(params.toString())
            .execute()

        if (!httpResponse.isOk) {
            return null
        }
        val maimaiPlayerData = JSONUtil.toBean(httpResponse.body(), DivingFishMaimaiPlayerData::class.java)
//        JSON.toJavaObject(JSON.parseObject(httpResponse.body()), DivingFishMaimaiPlayerData::class.java)
        return maimaiPlayerData
    }

    override suspend fun b50() {
        val playerData = this.queryPlayerData("qq", 694372459L)
//        val byteArr = maimaiProberImageDrawer.generateBest(playerData!!)
//        FileUtil.writeBytes(byteArr, "/Users/crazykid/Downloads/1.png")

    }
}



