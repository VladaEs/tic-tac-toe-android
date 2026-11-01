package tic.tac.toe.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random
import tic.tac.toe.model.Difficulty
import tic.tac.toe.model.CellPosition
import tic.tac.toe.model.GameSettings
import tic.tac.toe.model.Mark
import tic.tac.toe.model.RoundOutcome
import tic.tac.toe.model.SeriesScore
import tic.tac.toe.model.SideChoice

class GameSession(
    val settings: GameSettings,
    private val random: Random = Random.Default,
) {
    var board by mutableStateOf(GameEngine.emptyBoard(settings.boardConfig))
        private set

    var playerMark by mutableStateOf(resolvePlayerMark())
        private set

    var isAndroidTurn by mutableStateOf(playerMark == Mark.O)
        private set

    var outcome by mutableStateOf<RoundOutcome?>(null)
        private set

    var score by mutableStateOf(SeriesScore())
        private set

    var roundId by mutableIntStateOf(0)
        private set

    fun playPlayerMove(position: CellPosition) {
        if (isAndroidTurn || outcome != null) return

        val updatedBoard = GameEngine.tryPlaceMark(board, position, playerMark) ?: return
        board = updatedBoard
        if (!finishRoundIfNeeded(position)) {
            isAndroidTurn = true
        }
    }

    fun playAndroidMove() {
        if (!isAndroidTurn || outcome != null) return

        val androidMark = playerMark.opposite()
        val move = when (settings.difficulty) {
            Difficulty.BEGINNER -> GameEngine.chooseBeginnerMove(board, random)
            Difficulty.EXPERIENCED -> GameEngine.chooseExperiencedMove(
                board = board,
                androidMark = androidMark,
                random = random,
            )
            Difficulty.EXPERT -> null
        }

        if (move != null) {
            board = GameEngine.tryPlaceMark(board, move, androidMark) ?: board
        }
        if (move == null || !finishRoundIfNeeded(move)) {
            isAndroidTurn = false
        }
    }

    fun continueWithNextRound() {
        board = GameEngine.emptyBoard(settings.boardConfig)
        playerMark = resolvePlayerMark()
        outcome = null
        isAndroidTurn = playerMark == Mark.O
        roundId += 1
    }

    private fun finishRoundIfNeeded(lastMove: CellPosition): Boolean {
        val winner = GameEngine.winner(board, lastMove)
        val completedOutcome = when {
            winner == playerMark -> RoundOutcome.PLAYER_WIN
            winner == playerMark.opposite() -> RoundOutcome.ANDROID_WIN
            GameEngine.isFull(board) -> RoundOutcome.DRAW
            else -> null
        } ?: return false

        outcome = completedOutcome
        isAndroidTurn = false
        score = when (completedOutcome) {
            RoundOutcome.PLAYER_WIN -> score.copy(
                totalGames = score.totalGames + 1,
                playerWins = score.playerWins + 1,
            )
            RoundOutcome.ANDROID_WIN -> score.copy(
                totalGames = score.totalGames + 1,
                androidWins = score.androidWins + 1,
            )
            RoundOutcome.DRAW -> score.copy(totalGames = score.totalGames + 1)
        }
        return true
    }

    private fun resolvePlayerMark(): Mark = when (settings.sideChoice) {
        SideChoice.X -> Mark.X
        SideChoice.O -> Mark.O
        SideChoice.RANDOM -> if (random.nextBoolean()) Mark.X else Mark.O
    }
}
