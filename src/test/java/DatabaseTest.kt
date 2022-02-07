import cn.crazykid.qqrobot.Starter
import cn.crazykid.qqrobot.dao.intf.ArcadeDao
import cn.crazykid.qqrobot.wrapper.ArcadeQuery
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

/**
 * @author CrazyKid
 * @date 2021/12/30 09:45
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [Starter::class])
class DatabaseTest {
    @Autowired
    private lateinit var arcadeDao: ArcadeDao

    @Test
    fun testDao() {
        println(arcadeDao.mapper().listEntity(ArcadeQuery()))
    }
}
