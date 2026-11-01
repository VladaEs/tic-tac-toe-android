package tic.tac.toe.model

enum class Mark {
    X,
    O;

    fun opposite(): Mark = if (this == X) O else X
}

enum class Difficulty {
    BEGINNER,
    EXPERIENCED,
}

enum class SideChoice {
    X,
    O,
    RANDOM,
}

enum class RoundOutcome {
    PLAYER_WIN,
    ANDROID_WIN,
    DRAW,
}

data class GameSettings(
    val playerName: String,
    val difficulty: Difficulty,
    val sideChoice: SideChoice,
    val boardConfig: BoardConfig = BoardConfig.CLASSIC,
)

data class BoardConfig(
    val width: Int,
    val height: Int,
    val marksToWin: Int,
) {
    init {
        require(width > 0) { "Board width must be positive" }
        require(height > 0) { "Board height must be positive" }
        require(marksToWin > 0) { "Marks to win must be positive" }
        require(marksToWin <= maxOf(width, height)) {
            "Marks to win must fit at least one board direction"
        }
        require(width <= Int.MAX_VALUE / height) { "Board is too large" }
    }

    val cellCount: Int
        get() = width * height

    companion object {
        val CLASSIC = BoardConfig(width = 3, height = 3, marksToWin = 3)
    }
}

data class CellPosition(
    val row: Int,
    val column: Int,
)

data class BoardState(
    val config: BoardConfig,
    val cells: List<Mark?> = List(config.cellCount) { null },
) {
    init {
        require(cells.size == config.cellCount) {
            "Expected ${config.cellCount} cells, but received ${cells.size}"
        }
    }

    val width: Int
        get() = config.width

    val height: Int
        get() = config.height

    val marksToWin: Int
        get() = config.marksToWin

    fun contains(position: CellPosition): Boolean =
        position.row in 0 until height && position.column in 0 until width

    fun indexOf(position: CellPosition): Int = position.row * width + position.column

    fun positionOf(index: Int): CellPosition? = index
        .takeIf { it in cells.indices }
        ?.let { CellPosition(row = it / width, column = it % width) }

    operator fun get(position: CellPosition): Mark? =
        if (contains(position)) cells[indexOf(position)] else null
}

data class SeriesScore(
    val totalGames: Int = 0,
    val playerWins: Int = 0,
    val androidWins: Int = 0,
) {
    val draws: Int
        get() = totalGames - playerWins - androidWins
}
