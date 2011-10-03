package com.curiousattemptbunny.jai.chess

import java.awt.event.MouseMotionListener
import java.awt.event.MouseListener
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Canvas
import javax.media.jai.PlanarImage
import java.awt.geom.AffineTransform

/**
 * Created by IntelliJ IDEA.
 * User: merlyna
 * Date: 9/9/11
 * Time: 9:51 PM
 * To change this template use File | Settings | File Templates.
 */
class ConfigureBoard {
    def exampleGraphics
    def sourceGraphics
    def coords
    def sourceImage

    def configure() {
        sourceImage.show { Canvas canvas, Graphics2D g ->
            sourceGraphics = g
            redraw()

            def dragging = null

            canvas.addMouseListener(
                [mousePressed: { e ->
                    def dist = { Math.abs(e.x-it.x) + Math.abs(e.y-it.y) }
                    dragging = coords.min(dist)
                    if (dist(dragging) > 50) {
                        dragging = null
                    }
                },
                mouseClicked: {
                    coords << coords.remove(0)
                    redraw()
//                    drawExample()
                },
                mouseReleased: {
//                    drawExample()
                },
                mouseEntered: {},
                mouseExited: {}] as MouseListener
            )

            canvas.addMouseMotionListener(
                [mouseMoved: {},
                mouseDragged: { e ->
                    if (dragging == null) return
                    dragging.x = e.x
                    dragging.y = e.y
                    redraw()
                    println "def coords = ["+coords.collect { "[x:$it.x, y:$it.y]" }.join(", ")+"]"
                }] as MouseMotionListener
            )
        }
    }

    def redraw() {
        sourceGraphics.setPaintMode()
        sourceGraphics.drawRenderedImage(sourceImage, new AffineTransform())
        sourceGraphics.setXORMode(Color.WHITE)
        sourceGraphics.drawLine(coords[0], coords[1])
        sourceGraphics.drawLine(coords[1], coords[2])
        sourceGraphics.drawLine(coords[2], coords[3])
        sourceGraphics.drawLine(coords[3], coords[0])
        sourceGraphics.drawString("a1", coords[0])
        sourceGraphics.drawString("h1", coords[1])
        sourceGraphics.drawString("h8", coords[2])
        sourceGraphics.drawString("a8", coords[3])
    }
}
