import cn.org.atool.generator.FileGenerator
import cn.org.atool.generator.annotation.Table
import cn.org.atool.generator.annotation.Tables
import org.junit.Test

/**
 * Fluent Mybatis 代码生成器
 *
 * @author CrazyKid
 * @date 2021/12/30 11:08
 */

class EntityGenerator {
    companion object {
        private const val url =
            "jdbc:mysql://127.0.0.1:3306/dbName?characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull&tinyInt1isBit=false&useSSL=false&allowMultiQueries=true"
        private const val username = "root"
        private const val password = "root"
    }

    @Test
    @Throws(Exception::class)
    fun generate() {
        // 引用配置类，build方法允许有多个配置类
        FileGenerator.build(Empty::class.java)
    }

    @Tables(
        url = url,
        username = username,
        password = password,
        srcDir = "src/main/java",
        basePack = "cn.crazykid.qqrobot",
        daoDir = "src/main/java",
        alphabetOrder = false,
        driver = "com.mysql.jdbc.Driver",
        entitySuffix = "",
        gmtCreated = "create_time",
        gmtModified = "update_time",
        logicDeleted = "delete_flag",
        version = "version",
        tables = [Table(value = ["tableName"])]
    )
    internal class Empty { //类名随便取, 只是配置定义的一个载体
    }
}
