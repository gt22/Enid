package tla.dice

import java.awt.Color
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

object ImageMaker {

    private val template by lazy { ImageIO.read(javaClass.getResourceAsStream("/die.png")) }
    private val templateFont by lazy {
        Font.createFont(Font.TRUETYPE_FONT, javaClass.getResourceAsStream("/JetBrainsMono-Regular.ttf"))
            .deriveFont(24f)
    }

    fun write(x: Int): BufferedImage {
        val res = BufferedImage(template.width, template.height, BufferedImage.TYPE_INT_ARGB)
        with(res.graphics) {
            drawImage(template, 0, 0, null)
            font = templateFont
            color = Color.WHITE
            val s = x.toString()
            drawString(s, (template.width - fontMetrics.stringWidth(s)) / 2, (template.height + 20)/ 2)
        }
        return res
    }

    fun BufferedImage.toByeArray(): ByteArray {
        val out = ByteArrayOutputStream()
        ImageIO.write(this, "png", out)
        return out.toByteArray()
    }
}