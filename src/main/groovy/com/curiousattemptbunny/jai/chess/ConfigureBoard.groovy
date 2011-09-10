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
    def coords
    def sourceImage

    def configure() {
        sourceImage.show { Canvas canvas, Graphics2D g ->
            def draw = {
                g.setXORMode(Color.WHITE)
                g.drawLine(coords[0], coords[1])
                g.drawLine(coords[1], coords[2])
                g.drawLine(coords[2], coords[3])
                g.drawLine(coords[3], coords[0])
                g.drawString("a1", coords[0])
                g.drawString("h1", coords[1])
                g.drawString("h8", coords[2])
                g.drawString("a8", coords[3])
            }

            def drawExample = {
                g.setXORMode(Color.WHITE)
                def example = sourceImage.toSquare(480,480,coords)
                exampleGraphics.setPaintMode()
                exampleGraphics.drawRenderedImage(example, new AffineTransform())
                exampleGraphics.setXORMode(Color.WHITE)
                def size = (int)(480/8)
                (1..7).each { x ->
                    exampleGraphics.drawLine(x*size, 0, x*size, 480)
                    exampleGraphics.drawLine(0, x*size, 480, x*size)
                }
            }

            draw()
            drawExample()

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
                    draw()
                    coords << coords.remove(0)
                    draw()
                    drawExample()
                },
                mouseReleased: {
                    drawExample()
                },
                mouseEntered: {},
                mouseExited: {}] as MouseListener
            )

            canvas.addMouseMotionListener(
                [mouseMoved: {},
                mouseDragged: { e ->
                    if (dragging == null) return
                    draw()
                    dragging.x = e.x
                    dragging.y = e.y
                    draw()
                }] as MouseMotionListener
            )
        }
    }
}
