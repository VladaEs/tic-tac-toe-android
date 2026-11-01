package tic.tac.toe.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import tic.tac.toe.model.Difficulty
import tic.tac.toe.model.GameSettings
import tic.tac.toe.model.SideChoice

private enum class AppScreen {
    MENU,
    GAME,
    RULES,
    ABOUT,
    EXPERT,
}

@Composable
fun TicTacToeApp() {
    var screenName by rememberSaveable { mutableStateOf(AppScreen.MENU.name) }
    var playerName by rememberSaveable { mutableStateOf("") }
    var difficultyName by rememberSaveable { mutableStateOf(Difficulty.BEGINNER.name) }
    var sideChoiceName by rememberSaveable { mutableStateOf(SideChoice.X.name) }
    var activeSettings by remember { mutableStateOf<GameSettings?>(null) }

    val screen = AppScreen.valueOf(screenName)
    val difficulty = Difficulty.valueOf(difficultyName)
    val sideChoice = SideChoice.valueOf(sideChoiceName)

    when (screen) {
        AppScreen.MENU -> MainMenuScreen(
            playerName = playerName,
            difficulty = difficulty,
            sideChoice = sideChoice,
            onPlayerNameChange = { playerName = it },
            onDifficultyChange = { difficultyName = it.name },
            onSideChoiceChange = { sideChoiceName = it.name },
            onStart = {
                val settings = GameSettings(
                    playerName = playerName.trim().ifBlank { "Player 1" },
                    difficulty = difficulty,
                    sideChoice = sideChoice,
                )
                if (difficulty == Difficulty.EXPERT) {
                    screenName = AppScreen.EXPERT.name
                } else {
                    activeSettings = settings
                    screenName = AppScreen.GAME.name
                }
            },
            onRules = { screenName = AppScreen.RULES.name },
            onAbout = { screenName = AppScreen.ABOUT.name },
        )

        AppScreen.GAME -> activeSettings?.let { settings ->
            GameScreen(
                settings = settings,
                onMainMenu = { screenName = AppScreen.MENU.name },
            )
        } ?: run {
            screenName = AppScreen.MENU.name
        }

        AppScreen.RULES -> InfoScreen(
            title = androidx.compose.ui.res.stringResource(tic.tac.toe.R.string.rules),
            body = androidx.compose.ui.res.stringResource(tic.tac.toe.R.string.rules_text),
            onBack = { screenName = AppScreen.MENU.name },
        )

        AppScreen.ABOUT -> InfoScreen(
            title = androidx.compose.ui.res.stringResource(tic.tac.toe.R.string.about),
            body = androidx.compose.ui.res.stringResource(tic.tac.toe.R.string.about_text),
            onBack = { screenName = AppScreen.MENU.name },
        )

        AppScreen.EXPERT -> InfoScreen(
            title = androidx.compose.ui.res.stringResource(tic.tac.toe.R.string.expert_title),
            body = androidx.compose.ui.res.stringResource(tic.tac.toe.R.string.expert_message),
            onBack = { screenName = AppScreen.MENU.name },
        )
    }
}
