import cn.crazykid.qqrobot.Starter
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.text.csv.CsvUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

/**
 * @author CrazyKid
 * @date 2022/1/13 16:55
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [Starter::class])
class CsvReadTest {

    @Test
    fun test() {
        var songAliases: MutableMap<String, MutableList<String>> = mutableMapOf()
        val reader = CsvUtil.getReader()

        val data = reader.readFromStr(ResourceUtil.readUtf8Str("aliases.csv"))
        val rows = data.rows
        //遍历行
        for (csvRow in rows) {
            //\t分割, 第一个是歌曲完整名称, 后面是别名
            for (str in csvRow.rawList) {
                val split = str.split("\t")
                if (split.size > 1) {
                    split.forEachIndexed { index, songName ->
                        if (index > 0) {
                            songAliases.merge(split[0], mutableListOf(songName)) { existed, replacement ->
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

        println(songAliases)
    }

}
