package tic.tac.toe.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tic.tac.toe.R
import tic.tac.toe.game.GameSession
import tic.tac.toe.model.BoardState
import tic.tac.toe.model.CellPosition
import tic.tac.toe.model.GameSettings
import tic.tac.toe.model.Mark
import tic.tac.toe.model.RoundOutcome

@Composable
fun GameScreen(
    settings: GameSettings,
    onMainMenu: () -> Unit,
) {
    val session = remember(settings) { GameSession(settings) }
    var showLeaveConfirmation by remember { mutableStateOf(false) }

    BackHandler {
        showLeaveConfirmation = true
    }

    LaunchedEffect(session.roundId, session.isAndroidTurn, session.outcome) {
        if (session.isAndroidTurn && session.outcome == null) {
            delay(350)
            session.playAndroidMove()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { showLeaveConfirmation = true }) {
                    Text(stringResource(R.string.main_menu))
                }
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = stringResource(
                    R.string.score_format,
                    settings.playerName,
                    session.score.playerWins,
                    session.score.androidWins,
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.player_side_format, session.playerMark.name),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.active_board_summary,
                    session.board.width,
                    session.board.height,
                    session.board.marksToWin,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                GameBoard(
                    board = session.board,
                    enabled = !session.isAndroidTurn && session.outcome == null,
                    onCellClick = session::playPlayerMove,
                    modifier = Modifier
                        .sizeIn(maxWidth = 360.dp, maxHeight = 360.dp)
                        .aspectRatio(session.board.width.toFloat() / session.board.height),
                )
            }

            Text(
                text = if (session.isAndroidTurn) {
                    stringResource(R.string.android_turn)
                } else {
                    stringResource(R.string.your_turn)
                },
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    session.outcome?.let { outcome ->
        RoundResultDialog(
            outcome = outcome,
            onContinue = session::continueWithNextRound,
            onMainMenu = onMainMenu,
        )
    }

    if (showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmation = false },
            title = { Text(stringResource(R.string.leave_game_title)) },
            text = { Text(stringResource(R.string.leave_game_message)) },
            confirmButton = {
                Button(onClick = onMainMenu) {
                    Text(stringResource(R.string.leave))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun GameBoard(
    board: BoardState,
    enabled: Boolean,
    onCellClick: (CellPosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val largestDimension = maxOf(board.width, board.height)
    val cellSpacing = when {
        largestDimension <= 3 -> 6.dp
        largestDimension <= 5 -> 4.dp
        else -> 2.dp
    }
    val markFontSize = when {
        largestDimension <= 3 -> 48.sp
        largestDimension <= 5 -> 34.sp
        largestDimension <= 8 -> 24.sp
        else -> 18.sp
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(cellSpacing),
    ) {
        repeat(board.height) { row ->
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(cellSpacing),
            ) {
                repeat(board.width) { column ->
                    val position = CellPosition(row = row, column = column)
                    val index = board.indexOf(position)
                    val mark = board[position]
                    val description = if (mark == null) {
                        stringResource(R.string.empty_cell_description, index + 1)
                    } else {
                        stringResource(R.string.marked_cell_description, index + 1, mark.name)
                    }

                    Surface(
                        onClick = { onCellClick(position) },
                        enabled = enabled && mark == null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics { contentDescription = description },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = mark?.name.orEmpty(),
                                fontSize = markFontSize,
                                fontWeight = FontWeight.Bold,
                                color = when (mark) {
                                    Mark.X -> MaterialTheme.colorScheme.primary
                                    Mark.O -> MaterialTheme.colorScheme.tertiary
                                    null -> MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundResultDialog(
    outcome: RoundOutcome,
    onContinue: () -> Unit,
    onMainMenu: () -> Unit,
) {
    val resultText = when (outcome) {
        RoundOutcome.PLAYER_WIN -> stringResource(R.string.you_win)
        RoundOutcome.ANDROID_WIN -> stringResource(R.string.android_wins)
        RoundOutcome.DRAW -> stringResource(R.string.draw)
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text(resultText) },
        confirmButton = {
            Button(onClick = onContinue) {
                Text(stringResource(R.string.continue_game))
            }
        },
        dismissButton = {
            TextButton(onClick = onMainMenu) {
                Text(stringResource(R.string.main_menu))
            }
        },
    )
}
