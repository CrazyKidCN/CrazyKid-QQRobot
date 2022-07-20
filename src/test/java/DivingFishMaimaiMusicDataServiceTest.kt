import cn.crazykid.qqrobot.Starter
import cn.crazykid.qqrobot.service.IMaimaiMusicDataService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

/**
 * @author CrazyKid
 * @date 2022/7/18 11:51
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [Starter::class])
class DivingFishMaimaiMusicDataServiceTest {
    @Autowired
    private lateinit var divingFishMaimaiMusicDataService: IMaimaiMusicDataService

    @Test
    fun test() {
    }
}
