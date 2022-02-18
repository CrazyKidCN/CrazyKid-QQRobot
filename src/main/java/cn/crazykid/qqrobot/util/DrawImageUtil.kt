package cn.crazykid.qqrobot.util

import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * 绘图工具类
 *
 * @author CrazyKid
 * @date 2021/11/19 11:48
 */
object DrawImageUtil {
    private fun getWidthAndHeight(text: String, font: Font): IntArray {
        val r: Rectangle2D = font.getStringBounds(
            text, FontRenderContext(
                AffineTransform.getScaleInstance(1.0, 1.0), false, false
            )
        )
        val unitHeight = Math.floor(r.getHeight()).toInt() //
        // 获取整个str用了font样式的宽度这里用四舍五入后+1保证宽度绝对能容纳这个字符串作为图片的宽度
        val width = Math.round(r.getWidth()).toInt() + 1
        // 把单个字符的高度+3保证高度绝对能容纳字符串作为图片的高度
        val height = unitHeight + 3
        return intArrayOf(width, height)
    }

    // 根据str,font的样式以及输出文件目录
    @Throws(Exception::class)
    @JvmStatic
    fun createImageByText(text: String, font: Font, outFile: File?) {
        val textArr = text.split("\n")
        // 行数
        val line = textArr.size

        // 每一行的宽高, 均取最大
        var width = 0
        var height = 0
        for (txt in textArr) {
            val arr = getWidthAndHeight(txt, font)
            if (arr[0] > width) {
                width = arr[0]
            }
            if (arr[1] > height) {
                height = arr[1]
            }
        }
        // 总共的高度, 根据行数
        val totalHeight = height * line

        // 创建图片
        var image = BufferedImage(
            width, totalHeight,
            BufferedImage.TYPE_INT_BGR
        ) //创建图片画布
        //透明背景  the begin
        var g = image.createGraphics()
        image = g.deviceConfiguration.createCompatibleImage(width, totalHeight, Transparency.TRANSLUCENT)
        g = image.createGraphics()
        // 解决字体模糊
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
        //透明背景  the end
        /**
         * 如果你需要白色背景或者其他颜色背景可以直接这么设置，其实就是满屏输出的颜色
         * 我这里上面设置了透明颜色，这里就不用了
         */
        //g.setColor(Color.WHITE);
        //画出矩形区域，以便于在矩形区域内写入文字
        g.fillRect(0, 0, width, totalHeight)
        /**
         * 文字颜色，这里支持RGB。new Color("red", "green", "blue", "alpha");
         * alpha 我没用好，有用好的同学可以在下面留言，我开始想用这个直接输出透明背景色，
         * 然后输出文字，达到透明背景效果，最后选择了，createCompatibleImage Transparency.TRANSLUCENT来创建。
         * android 用户有直接的背景色设置，Color.TRANSPARENT 可以看下源码参数。对alpha的设置
         */
        g.color = Color.gray
        // 设置画笔字体
        g.font = font

        var currentY = 0
        for (txt in textArr) {
            g.drawString(txt, 0, currentY + font.getSize())
            currentY += height
        }

        //执行处理
        g.dispose()
        // 输出png图片，formatName 对应图片的格式
        ImageIO.write(image, "png", outFile)
    }
}
