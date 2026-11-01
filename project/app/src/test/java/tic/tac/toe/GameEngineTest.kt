package tic.tac.toe

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tic.tac.toe.game.GameEngine
import tic.tac.toe.model.BoardConfig
import tic.tac.toe.model.BoardState
import tic.tac.toe.model.CellPosition
import tic.tac.toe.model.Mark

class GameEngineTest {
    @Test
    fun `empty classic board has nine available moves`() {
        val board = GameEngine.emptyBoard()

        assertEquals(3, board.width)
        assertEquals(3, board.height)
        assertEquals(3, board.marksToWin)
        assertEquals(9, GameEngine.availableMoves(board).size)
        assertNull(GameEngine.winner(board))
        assertFalse(GameEngine.isDraw(board))
    }

    @Test
    fun `occupied cell cannot be changed`() {
        val position = CellPosition(row = 1, column = 1)
        val board = GameEngine.tryPlaceMark(GameEngine.emptyBoard(), position, Mark.X)

        assertNotNull(board)
        assertNull(GameEngine.tryPlaceMark(board!!, position, Mark.O))
    }

    @Test
    fun `classic board detects all eight winning lines`() {
        val winningPositions = listOf(
            listOf(cell(0, 0), cell(0, 1), cell(0, 2)),
            listOf(cell(1, 0), cell(1, 1), cell(1, 2)),
            listOf(cell(2, 0), cell(2, 1), cell(2, 2)),
            listOf(cell(0, 0), cell(1, 0), cell(2, 0)),
            listOf(cell(0, 1), cell(1, 1), cell(2, 1)),
            listOf(cell(0, 2), cell(1, 2), cell(2, 2)),
            listOf(cell(0, 0), cell(1, 1), cell(2, 2)),
            listOf(cell(0, 2), cell(1, 1), cell(2, 0)),
        )

        winningPositions.forEach { positions ->
            val board = boardWithMarks(BoardConfig.CLASSIC, positions, Mark.X)

            assertEquals("Failed direction: $positions", Mark.X, GameEngine.winner(board))
        }
    }

    @Test
    fun `five by five board detects four consecutive marks away from an edge`() {
        val config = BoardConfig(width = 5, height = 5, marksToWin = 4)
        val positions = listOf(cell(4, 1), cell(3, 2), cell(2, 3), cell(1, 4))
        val board = boardWithMarks(config, positions, Mark.O)

        assertEquals(Mark.O, GameEngine.winner(board, lastMove = cell(2, 3)))
    }

    @Test
    fun `one hundred by one hundred board supports a five mark win`() {
        val config = BoardConfig(width = 100, height = 100, marksToWin = 5)
        val positions = (20..24).map { column -> cell(row = 50, column = column) }
        val board = boardWithMarks(config, positions, Mark.X)

        assertEquals(10_000, board.cells.size)
        assertEquals(Mark.X, GameEngine.winner(board, lastMove = cell(50, 24)))
    }

    @Test
    fun `separated marks do not count as a win`() {
        val config = BoardConfig(width = 5, height = 5, marksToWin = 4)
        val positions = listOf(cell(2, 0), cell(2, 1), cell(2, 3), cell(2, 4))
        val board = boardWithMarks(config, positions, Mark.X)

        assertNull(GameEngine.winner(board))
    }

    @Test
    fun `full board without a winner is a draw`() {
        val board = BoardState(
            config = BoardConfig.CLASSIC,
            cells = listOf(
                Mark.X, Mark.O, Mark.X,
                Mark.O, Mark.O, Mark.X,
                Mark.X, Mark.X, Mark.O,
            ),
        )

        assertNull(GameEngine.winner(board))
        assertTrue(GameEngine.isDraw(board))
    }

    @Test
    fun `beginner move always uses an empty cell`() {
        val board = BoardState(
            config = BoardConfig.CLASSIC,
            cells = listOf(
                Mark.X, Mark.O, Mark.X,
                Mark.O, null, Mark.X,
                Mark.X, Mark.O, null,
            ),
        )
        val expectedMoves = setOf(cell(1, 1), cell(2, 2))

        repeat(20) { seed ->
            val move = GameEngine.chooseBeginnerMove(board, Random(seed))
            assertTrue(move in expectedMoves)
        }
    }

    @Test
    fun `experienced takes an immediate winning move`() {
        val board = BoardState(
            config = BoardConfig.CLASSIC,
            cells = listOf(
                Mark.X, Mark.X, null,
                Mark.O, Mark.O, null,
                null, null, null,
            ),
        )

        val move = GameEngine.chooseExperiencedMove(board, androidMark = Mark.X)

        assertEquals(cell(0, 2), move)
    }

    @Test
    fun `experienced never ignores a winning move when error chance is zero`() {
        val board = BoardState(
            config = BoardConfig.CLASSIC,
            cells = listOf(
                Mark.X, Mark.X, null,
                Mark.O, null, null,
                null, null, null,
            ),
        )

        repeat(100) { seed ->
            val move = GameEngine.chooseExperiencedMove(
                board = board,
                androidMark = Mark.X,
                errorChance = 0.0,
                random = Random(seed),
            )

            assertEquals("Failed for random seed $seed", cell(0, 2), move)
        }
    }

    @Test
    fun `experienced blocks an immediate loss`() {
        val board = BoardState(
            config = BoardConfig.CLASSIC,
            cells = listOf(
                Mark.X, Mark.X, null,
                Mark.O, null, null,
                null, null, null,
            ),
        )

        val move = GameEngine.chooseExperiencedMove(board, androidMark = Mark.O)

        assertEquals(cell(0, 2), move)
    }

    @Test
    fun `experienced uses dynamic win length on a five by five board`() {
        val config = BoardConfig(width = 5, height = 5, marksToWin = 4)
        val board = boardWithMarks(
            config = config,
            positions = listOf(cell(2, 0), cell(2, 1), cell(2, 2)),
            mark = Mark.X,
        )

        val move = GameEngine.chooseExperiencedMove(board, androidMark = Mark.O)

        assertEquals(cell(2, 3), move)
    }

    @Test
    fun `experienced prefers its win over blocking the player`() {
        val board = BoardState(
            config = BoardConfig.CLASSIC,
            cells = listOf(
                Mark.X, Mark.X, null,
                Mark.O, Mark.O, null,
                null, null, null,
            ),
        )

        val move = GameEngine.chooseExperiencedMove(board, androidMark = Mark.O)

        assertEquals(cell(1, 2), move)
    }

    @Test
    fun `experienced uses a valid random fallback when there is no immediate threat`() {
        val board = boardWithMarks(BoardConfig.CLASSIC, listOf(cell(1, 1)), Mark.X)

        val move = GameEngine.chooseExperiencedMove(
            board = board,
            androidMark = Mark.O,
            random = Random(1),
        )

        assertNotNull(move)
        assertNull(board[move!!])
    }

    private fun boardWithMarks(
        config: BoardConfig,
        positions: List<CellPosition>,
        mark: Mark,
    ): BoardState {
        var board = GameEngine.emptyBoard(config)
        positions.forEach { position ->
            board = GameEngine.tryPlaceMark(board, position, mark)!!
        }
        return board
    }

    private fun cell(row: Int, column: Int) = CellPosition(row, column)
}
