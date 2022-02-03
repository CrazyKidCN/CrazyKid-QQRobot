import cn.crazykid.qqrobot.Starter
import cn.crazykid.qqrobot.entity.WahlapMaimaiArcade
import cn.hutool.http.HttpUtil
import com.alibaba.fastjson.JSON
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

/**
 * @author CrazyKid
 * @date 2022/1/29 10:46
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [Starter::class])
class ArcadeListGetTest {

    @Test
    fun test() {
        val json = HttpUtil.get("http://wc.wahlap.net/maidx/rest/location")
        val arcadeList = JSON.parseArray(json, WahlapMaimaiArcade::class.java)
        println(arcadeList)
    }

}
