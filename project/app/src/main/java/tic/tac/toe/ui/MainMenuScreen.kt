package tic.tac.toe.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import tic.tac.toe.R
import tic.tac.toe.model.BoardConfig
import tic.tac.toe.model.Difficulty
import tic.tac.toe.model.SideChoice

private const val MIN_BOARD_SIZE = 3
private const val MAX_BOARD_SIZE = 10
private const val MIN_MARKS_TO_WIN = 3
private const val MAX_NUMBER_LENGTH = 2

@Composable
fun MainMenuScreen(
    playerName: String,
    difficulty: Difficulty,
    sideChoice: SideChoice,
    boardWidth: String,
    boardHeight: String,
    marksToWin: String,
    onPlayerNameChange: (String) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onSideChoiceChange: (SideChoice) -> Unit,
    onBoardWidthChange: (String) -> Unit,
    onBoardHeightChange: (String) -> Unit,
    onMarksToWinChange: (String) -> Unit,
    onStart: (BoardConfig) -> Unit,
    onRules: () -> Unit,
    onAbout: () -> Unit,
) {
    val validWidth = boardWidth.toIntOrNull()
        ?.takeIf { it in MIN_BOARD_SIZE..MAX_BOARD_SIZE }
    val validHeight = boardHeight.toIntOrNull()
        ?.takeIf { it in MIN_BOARD_SIZE..MAX_BOARD_SIZE }
    val maximumWinningLength = if (validWidth != null && validHeight != null) {
        maxOf(validWidth, validHeight)
    } else {
        null
    }
    val validMarksToWin = marksToWin.toIntOrNull()?.takeIf { requiredMarks ->
        maximumWinningLength != null &&
            requiredMarks in MIN_MARKS_TO_WIN..maximumWinningLength
    }
    val boardConfig = if (
        validWidth != null &&
        validHeight != null &&
        validMarksToWin != null
    ) {
        BoardConfig(
            width = validWidth,
            height = validHeight,
            marksToWin = validMarksToWin,
        )
    } else {
        null
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.app_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = playerName,
                onValueChange = onPlayerNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.player_name)) },
                placeholder = { Text(stringResource(R.string.player_name_hint)) },
                singleLine = true,
            )

            Spacer(Modifier.height(24.dp))

            SelectionGroupTitle(stringResource(R.string.board_setup))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BoardNumberField(
                    value = boardWidth,
                    onValueChange = { onBoardWidthChange(sanitizeNumberInput(it)) },
                    label = stringResource(R.string.board_width),
                    isError = validWidth == null,
                    supportingText = stringResource(
                        R.string.number_range,
                        MIN_BOARD_SIZE,
                        MAX_BOARD_SIZE,
                    ),
                    modifier = Modifier.weight(1f),
                )
                BoardNumberField(
                    value = boardHeight,
                    onValueChange = { onBoardHeightChange(sanitizeNumberInput(it)) },
                    label = stringResource(R.string.board_height),
                    isError = validHeight == null,
                    supportingText = stringResource(
                        R.string.number_range,
                        MIN_BOARD_SIZE,
                        MAX_BOARD_SIZE,
                    ),
                    modifier = Modifier.weight(1f),
                )
                BoardNumberField(
                    value = marksToWin,
                    onValueChange = { onMarksToWinChange(sanitizeNumberInput(it)) },
                    label = stringResource(R.string.marks_to_win),
                    isError = validMarksToWin == null,
                    supportingText = maximumWinningLength?.let { maximum ->
                        stringResource(R.string.number_range, MIN_MARKS_TO_WIN, maximum)
                    } ?: stringResource(R.string.select_board_size_first),
                    modifier = Modifier.weight(1f),
                )
            }

            boardConfig?.let { config ->
                Text(
                    text = stringResource(
                        R.string.board_setup_summary,
                        config.width,
                        config.height,
                        config.cellCount,
                        config.marksToWin,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(20.dp))

            SelectionGroupTitle(stringResource(R.string.difficulty))
            Difficulty.entries.forEach { option ->
                val title = when (option) {
                    Difficulty.BEGINNER -> stringResource(R.string.beginner)
                    Difficulty.EXPERIENCED -> stringResource(R.string.experienced)
                }
                val description = when (option) {
                    Difficulty.BEGINNER -> stringResource(R.string.beginner_description)
                    Difficulty.EXPERIENCED -> stringResource(R.string.experienced_description)
                }
                ChoiceRow(
                    title = title,
                    description = description,
                    selected = difficulty == option,
                    onSelect = { onDifficultyChange(option) },
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(12.dp))

            SelectionGroupTitle(stringResource(R.string.choose_side))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SideChoice.entries.forEach { option ->
                    val label = when (option) {
                        SideChoice.X -> "X"
                        SideChoice.O -> "O"
                        SideChoice.RANDOM -> stringResource(R.string.random)
                    }
                    CompactChoice(
                        title = label,
                        selected = sideChoice == option,
                        onSelect = { onSideChoiceChange(option) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { boardConfig?.let(onStart) },
                enabled = boardConfig != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.start))
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onRules,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.rules))
                }
                OutlinedButton(
                    onClick = onAbout,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.about))
                }
            }
        }
    }
}

@Composable
private fun BoardNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    supportingText: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        supportingText = { Text(supportingText) },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

private fun sanitizeNumberInput(value: String): String = value
    .filter(Char::isDigit)
    .take(MAX_NUMBER_LENGTH)

@Composable
private fun SelectionGroupTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ChoiceRow(
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = MaterialTheme.shapes.medium,
        color = color,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CompactChoice(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = modifier.clickable(onClick = onSelect),
        shape = MaterialTheme.shapes.medium,
        color = color,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Text(text = title, style = MaterialTheme.typography.labelLarge)
        }
    }
}
