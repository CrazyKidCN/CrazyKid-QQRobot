import cn.crazykid.qqrobot.Starter
import cn.crazykid.qqrobot.service.IMaimaiProberService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

/**
 * @author CrazyKid
 * @date 2023/7/27 10:40
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [Starter::class])
class MaimaiProberServiceTest {
    @Autowired
    private lateinit var maimaiProberService: IMaimaiProberService

    @Test
    fun testQueryPlayerData() {
        val maimaiPlayerData = maimaiProberService.queryPlayerData("qq", 694372459L)
        println(maimaiPlayerData)
    }
}
