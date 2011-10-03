package com.curiousattemptbunny.jai.chess

import com.nullprogram.chess.Move
import com.nullprogram.chess.Piece
import com.nullprogram.chess.boards.StandardBoard

class ReplayGame {
    static void main(String[] args) {
        new MetaMethods() // registers methods

//        def coords = [[x:555, y:463], [x:559, y:57], [x:144, y:53], [x:153, y:475]]
        //def sourceImage = new File("data/2D/001 - initial.png").image //PlanarImage.capture
        //def exampleImage = sourceImage.toSquare(384,384,coords)
        //def exampleGraphics
        //exampleImage.show { exampleGraphics = it }
        //new ConfigureBoard(coords:coords, sourceImage:sourceImage, exampleGraphics:exampleGraphics).configure()

        def files = (new File("data/2D/game_full_vs_suresh").listFiles() as List).sort().findAll { it.name.startsWith("s0") }.collect { it.image }
        def pre = files.remove(0)
        def board = new StandardBoard()
        def turn = Piece.Side.WHITE

        def determinator = new MoveDeterminator(board:board)

        while(files) {
            def post = files.remove(0)
            Move move = determinator.move(pre, post, turn)
            println move
            Piece piece = board.getPiece(move.origin)
            "say ${piece.class.simpleName} $move ${move.captured ? 'takes '+move.captured.class.simpleName : ''}".execute().waitFor()
            board.move(move)
            turn = (turn == Piece.Side.WHITE ? Piece.Side.BLACK : Piece.Side.WHITE)
            pre = post
        }
    }
}
