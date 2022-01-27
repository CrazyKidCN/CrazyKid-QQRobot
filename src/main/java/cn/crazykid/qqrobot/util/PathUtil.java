package cn.crazykid.qqrobot.util;

import cn.crazykid.qqrobot.Starter;
import cn.hutool.core.io.resource.ClassPathResource;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author CrazyKid
 * @since 2020/12/28 20:00
 */
public class PathUtil {
    public static <T> String getClassPath(Class<T> clazz) {
        if (clazz == null) {
            clazz = (Class<T>) Starter.class;
        }
        // 获取 jar 包位置, 该位置还包含 jar 的文件名本身.
        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        if (path.endsWith(".jar")) {
            path = path.substring(0, path.lastIndexOf("/") + 1);
        }
        return path;
    }

    public static String getClassPath() {
        return PathUtil.getClassPath(null);
    }

    public static InputStream getResource(String resourcePath) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        return resource.getStream();
    }
}
