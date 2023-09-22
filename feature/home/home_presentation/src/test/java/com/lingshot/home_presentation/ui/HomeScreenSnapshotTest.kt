@file:OptIn(ExperimentalCoroutinesApi::class)

package com.lingshot.home_presentation.ui

import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.lingshot.domain.model.GoalsDomain
import com.lingshot.domain.model.UserDomain
import com.lingshot.domain.model.UserLocalDomain
import com.lingshot.domain.model.statusSuccess
import com.lingshot.home_presentation.HomeUiState
import com.lingshot.home_presentation.navigation.HomeDestination
import com.lingshot.testing.helper.BASE_URL_COIL
import com.lingshot.testing.helper.DefaultTestDevices
import com.lingshot.testing.helper.MultiTheme
import com.lingshot.testing.helper.replaceCompileSdkToSnapshot
import com.lingshot.testing.helper.snapshotMultiDevice
import com.lingshot.testing.helper.startCoilFakeImage
import com.lingshot.testing.rule.MainCoroutineRule
import com.phrase.phrasemaster_domain.model.CollectionInfoDomain
import com.phrase.phrasemaster_domain.model.LanguageCollectionDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class HomeScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        environment = replaceCompileSdkToSnapshot()
    )

    @get:Rule
    val mainCoroutineRule: MainCoroutineRule = MainCoroutineRule(
        testDispatcher = UnconfinedTestDispatcher()
    )

    @Before
    fun setup() {
        startCoilFakeImage(paparazzi.context)
    }

    @Test
    fun homeScreen_Displayed_By_Default_And_Loading(
        @TestParameter defaultTestDevices: DefaultTestDevices,
        @TestParameter multiTheme: MultiTheme
    ) {
        paparazzi.snapshotMultiDevice(
            defaultTestDevices = defaultTestDevices,
            multiTheme = multiTheme
        ) {
            HomeScreen(
                homeDestination = HomeDestination(),
                handleEvent = {},
                uiState = HomeUiState()
            )
        }
    }

    @Test
    fun homeScreen_Displayed_By_Default_And_Success(
        @TestParameter defaultTestDevices: DefaultTestDevices,
        @TestParameter multiTheme: MultiTheme
    ) {
        paparazzi.snapshotMultiDevice(
            defaultTestDevices = defaultTestDevices,
            multiTheme = multiTheme
        ) {
            HomeScreen(
                homeDestination = HomeDestination(),
                handleEvent = {},
                uiState = fakeUiStateSuccess()
            )
        }
    }

    @Test
    fun homeScreen_Displayed_By_Default_And_Goals_Dialog_Visible(
        @TestParameter defaultTestDevices: DefaultTestDevices,
        @TestParameter multiTheme: MultiTheme
    ) {
        paparazzi.snapshotMultiDevice(
            defaultTestDevices = defaultTestDevices,
            multiTheme = multiTheme
        ) {
            HomeScreen(
                homeDestination = HomeDestination(),
                handleEvent = {},
                uiState = fakeUiStateSuccess().copy(isSetGoalsDialogVisible = true)
            )
        }
    }

    private fun fakeUiStateSuccess() =
        HomeUiState(
            userDomain = UserDomain(
                username = "UserTest2",
                profilePictureUrl = BASE_URL_COIL
            ),
            isPieChartGoalsVisible = true,
            languageCollectionsStatus = statusSuccess(
                Pair(
                    listOf(LanguageCollectionDomain(from = "en", to = "pt")),
                    CollectionInfoDomain(listOf(10), listOf(10))
                )
            ),
            phrasesPendingReviewStatus = statusSuccess("1"),
            consecutiveDaysStatus = statusSuccess(1),
            goals = Pair(
                UserLocalDomain(goal = 10),
                GoalsDomain(progressPhrases = 5)
            )
        )
}
