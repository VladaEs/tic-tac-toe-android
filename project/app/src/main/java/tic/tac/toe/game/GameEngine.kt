package tic.tac.toe.game

import kotlin.random.Random
import tic.tac.toe.model.BoardConfig
import tic.tac.toe.model.BoardState
import tic.tac.toe.model.CellPosition
import tic.tac.toe.model.Mark

object GameEngine {
    private data class Direction(
        val rowStep: Int,
        val columnStep: Int,
    )

    private val directions = listOf(
        Direction(rowStep = 0, columnStep = 1),
        Direction(rowStep = 1, columnStep = 0),
        Direction(rowStep = 1, columnStep = 1),
        Direction(rowStep = 1, columnStep = -1),
    )

    fun emptyBoard(config: BoardConfig = BoardConfig.CLASSIC): BoardState = BoardState(config)

    fun tryPlaceMark(
        board: BoardState,
        position: CellPosition,
        mark: Mark,
    ): BoardState? {
        if (!board.contains(position) || board[position] != null) return null

        val updatedCells = board.cells.toMutableList().apply {
            this[board.indexOf(position)] = mark
        }
        return board.copy(cells = updatedCells)
    }

    fun winner(board: BoardState, lastMove: CellPosition? = null): Mark? {
        if (lastMove != null) return winningMarkAt(board, lastMove)

        return board.cells.indices.firstNotNullOfOrNull { index ->
            winningMarkAt(board, board.positionOf(index)!!)
        }
    }

    fun availableMoves(board: BoardState): List<CellPosition> = board.cells.indices
        .asSequence()
        .filter { board.cells[it] == null }
        .map { board.positionOf(it)!! }
        .toList()

    fun isFull(board: BoardState): Boolean = board.cells.none { it == null }

    fun isDraw(board: BoardState): Boolean = winner(board) == null && isFull(board)

    fun chooseBeginnerMove(
        board: BoardState,
        random: Random = Random.Default,
    ): CellPosition? =
        availableMoves(board).randomOrNull(random)

    fun chooseExperiencedMove(
        board: BoardState,
        androidMark: Mark,
        random: Random = Random.Default,
    ): CellPosition? {
        return findImmediateWinningMove(board, androidMark)
            ?: findImmediateWinningMove(board, androidMark.opposite())
            ?: chooseBeginnerMove(board, random)
    }

    private fun findImmediateWinningMove(
        board: BoardState,
        mark: Mark,
    ): CellPosition? = availableMoves(board).firstOrNull { position ->
        isWinningMove(board, position, mark)
    }

    private fun isWinningMove(
        board: BoardState,
        position: CellPosition,
        mark: Mark,
    ): Boolean {
        if (!board.contains(position) || board[position] != null) return false
        return hasWinningDirection(board, position, mark)
    }

    private fun winningMarkAt(board: BoardState, position: CellPosition): Mark? {
        val mark = board[position] ?: return null
        return mark.takeIf { hasWinningDirection(board, position, mark) }
    }

    private fun hasWinningDirection(
        board: BoardState,
        position: CellPosition,
        mark: Mark,
    ): Boolean = directions.any { direction ->
        val forwardCount = countMatchingMarks(board, position, direction, mark)
        val backwardCount = countMatchingMarks(
            board = board,
            start = position,
            direction = Direction(-direction.rowStep, -direction.columnStep),
            mark = mark,
        )
        1 + forwardCount + backwardCount >= board.marksToWin
    }

    private fun countMatchingMarks(
        board: BoardState,
        start: CellPosition,
        direction: Direction,
        mark: Mark,
    ): Int {
        var count = 0
        var position = CellPosition(
            row = start.row + direction.rowStep,
            column = start.column + direction.columnStep,
        )

        while (board.contains(position) && board[position] == mark) {
            count += 1
            position = CellPosition(
                row = position.row + direction.rowStep,
                column = position.column + direction.columnStep,
            )
        }
        return count
    }
}
