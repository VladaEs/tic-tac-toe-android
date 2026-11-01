package tic.tac.toe.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import tic.tac.toe.R
import tic.tac.toe.model.Difficulty
import tic.tac.toe.model.SideChoice

@Composable
fun MainMenuScreen(
    playerName: String,
    difficulty: Difficulty,
    sideChoice: SideChoice,
    onPlayerNameChange: (String) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onSideChoiceChange: (SideChoice) -> Unit,
    onStart: () -> Unit,
    onRules: () -> Unit,
    onAbout: () -> Unit,
) {
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

            SelectionGroupTitle(stringResource(R.string.difficulty))
            Difficulty.entries.forEach { option ->
                val title = when (option) {
                    Difficulty.BEGINNER -> stringResource(R.string.beginner)
                    Difficulty.EXPERIENCED -> stringResource(R.string.experienced)
                    Difficulty.EXPERT -> stringResource(R.string.expert)
                }
                val description = when (option) {
                    Difficulty.BEGINNER -> stringResource(R.string.beginner_description)
                    Difficulty.EXPERIENCED -> stringResource(R.string.experienced_description)
                    Difficulty.EXPERT -> stringResource(R.string.expert_description)
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
                onClick = onStart,
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
