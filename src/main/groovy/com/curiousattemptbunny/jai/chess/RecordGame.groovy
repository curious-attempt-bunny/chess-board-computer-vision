package com.curiousattemptbunny.jai.chess

import javax.media.jai.PlanarImage
import java.awt.Canvas
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.Color
import java.awt.Graphics2D
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener

class RecordGame {
    static void main(String[] args) {
        new MetaMethods() // registers methods

        def coords = [[x:22, y:412], [x:620, y:406], [x:150, y:171], [x:488, y:167]]
        def sourceImage = PlanarImage.capture
        def exampleImage = sourceImage.toSquare(480,480,coords)
        def exampleGraphics
        exampleImage.show { exampleGraphics = it }
        new ConfigureBoard(coords:coords, sourceImage:sourceImage, exampleGraphics:exampleGraphics).configure()
    }
}
