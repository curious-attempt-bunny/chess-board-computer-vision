package com.curiousattemptbunny.jai.chess

import javax.media.jai.PlanarImage
import com.nullprogram.chess.boards.StandardBoard
import com.nullprogram.chess.Piece.Side
import com.nullprogram.chess.MoveList
import com.nullprogram.chess.Position
import java.awt.image.Raster
import com.nullprogram.chess.Board
import com.nullprogram.chess.Move
import com.nullprogram.chess.Piece

/**
 * Created by IntelliJ IDEA.
 * User: merlyna
 * Date: 9/10/11
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
class MoveDeterminator {
    def coords
    Board board

    Move move(PlanarImage from, PlanarImage to, Piece.Side turn) {
        from = normalize(from)
        to = normalize(to)

        Map<Position, Long> fromSquares = allActivity(to - from)
        Map<Position, Long> toSquares = allActivity(from - to)

        MoveList moves = board.allMoves(turn, true)

        def move = moves.max { move ->
            def val = (fromSquares[move.origin]/1000) * (toSquares[move.dest]/1000)
//            println( "$move -> $val")
            val
        }

        move
    }

    PlanarImage normalize(image) {
        //image.toSquare(384,384,coords)
        image
    }

    Map<Position, Long> allActivity(PlanarImage image) {
        int size = (int)(384/8)
        Raster raster = image.grayScale.data

        def positionToWeight = [:]
        (0..7).each { cx ->
            (0..7).each { cy ->
                long weight = (0..size-1).sum { x ->
                    (0..size-1).sum { y ->
                        def val = (long)raster.getSample((cx*size)+x,(cy*size)+y,0)
//                        println val
                        val
                    }
                }
//                println "$cx, $cy ${new Position(cx, 7-cy)} --> $weight"
                positionToWeight[new Position(cx, 7-cy)] = weight
            }
        }

        positionToWeight
    }
}
