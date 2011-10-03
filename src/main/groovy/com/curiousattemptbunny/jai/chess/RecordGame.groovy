package com.curiousattemptbunny.jai.chess

import javax.media.jai.PlanarImage
import java.awt.Canvas
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.Color
import java.awt.Graphics2D
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import com.nullprogram.chess.boards.StandardBoard
import com.nullprogram.chess.Piece
import com.nullprogram.chess.Move
import java.text.SimpleDateFormat
import java.awt.geom.AffineTransform

class RecordGame {
    static void main(String[] args) {
        new MetaMethods() // registers methods

        def coords = [[x:555, y:463], [x:559, y:57], [x:144, y:53], [x:153, y:475]]

        def sourceImage
        def exampleImage
        def exampleGraphics
        def configureBoard

        def updateImage = {
            try {
                sourceImage = PlanarImage.capture
                exampleImage = sourceImage.toSquare(384,384,coords)

                if (exampleGraphics) {
                    exampleGraphics.setPaintMode()
                    exampleGraphics.drawRenderedImage(exampleImage, new AffineTransform())
                    exampleGraphics.setXORMode(Color.WHITE)
                    def size = (int)(384/8)
                    (1..7).each { x ->
                        exampleGraphics.drawLine(x*size, 0, x*size, 384)
                        exampleGraphics.drawLine(0, x*size, 384, x*size)
                    }
                }
                if (configureBoard) {
                    configureBoard.sourceImage = sourceImage
                    configureBoard.redraw()
                }
            } catch (IllegalArgumentException) {
                // suppress "Crop The rectangular crop area must not be outside the image."
            }
        }

        def keepCapturing = true
        def captureThread = Thread.start {
            while(keepCapturing) {
                println "Capturing..."
                updateImage()
                Thread.sleep(500)
            }
        }

        while(exampleImage == null) {
            Thread.sleep(50)
        }

        exampleImage.show { exampleGraphics = it }
        configureBoard = new ConfigureBoard(coords:coords, sourceImage:sourceImage, exampleGraphics:exampleGraphics)
        configureBoard.configure()

        def today = new Date();
        def folder = new File("data/2D/game_${today.year+1900}_${today.month+1}_${today.date}_"+new SimpleDateFormat("HHmm").format(today))
        folder.mkdir()
        def index = 0
        println("Press ENTER for initial capture")

        System.in.eachLine {
            if (keepCapturing) {
                keepCapturing = false
                captureThread.join()
            }
            updateImage()
            def frame = sourceImage
            frame.write(new File(folder, "r"+index.toString().padLeft(3, "0")+"-raw.jpg"))
            def properties = new Properties()
            properties.put("a1.x", coords[0].x.toString())
            properties.put("a1.y", coords[0].y.toString())
            properties.put("h1.x", coords[1].x.toString())
            properties.put("h1.y", coords[1].y.toString())
            properties.put("h8.x", coords[2].x.toString())
            properties.put("h8.y", coords[2].y.toString())
            properties.put("a8.x", coords[3].x.toString())
            properties.put("a8.y", coords[3].y.toString())
            properties.store(new File(folder, "p"+index.toString().padLeft(3, "0")+".properties").newWriter(), null)
            "say done".execute().waitFor()

            frame.toSquare(384,384,coords).write(new File(folder, "s"+index.toString().padLeft(3, "0")+"-square.jpg"))
            index++
            println("Press ENTER to capture the next move")
        }
    }
}
