package com.curiousattemptbunny.jai.chess

import java.awt.event.MouseMotionListener
import java.awt.event.MouseListener
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Canvas
import javax.media.jai.PlanarImage

/**
 * Created by IntelliJ IDEA.
 * User: merlyna
 * Date: 9/9/11
 * Time: 9:51 PM
 * To change this template use File | Settings | File Templates.
 */
class ConfigureBoard {
    def coords

    def configure() {
        PlanarImage.capture.show { Canvas canvas, Graphics2D g ->
            def draw = {
                g.setXORMode(Color.WHITE)
                g.drawLine(coords[0], coords[1])
                g.drawLine(coords[0], coords[2])
                g.drawLine(coords[1], coords[3])
                g.drawLine(coords[2], coords[3])
                g.drawString("a8", coords[0])
                g.drawString("h8", coords[1])
                g.drawString("a1", coords[2])
                g.drawString("h1", coords[3])
            }

            draw()

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
                    def pre = coords.clone()
                    coords.clear()
                    coords << pre[2] << pre[0] << pre[3] << pre[1]
                    draw()
                },
                mouseReleased: {},
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