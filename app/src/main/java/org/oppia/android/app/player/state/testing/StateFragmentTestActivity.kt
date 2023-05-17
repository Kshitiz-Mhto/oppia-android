package org.oppia.android.app.player.state.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.android.app.hintsandsolution.HintsAndSolutionListener
import org.oppia.android.app.hintsandsolution.RevealHintListener
import org.oppia.android.app.hintsandsolution.RevealSolutionInterface
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.audio.AudioButtonListener
import org.oppia.android.app.player.exploration.HintsAndSolutionExplorationManagerListener
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.player.state.testing.StateFragmentTestActivityPresenter.Companion.TEST_ACTIVITY_EXPLORATION_ID_EXTRA_KEY
import org.oppia.android.app.player.state.testing.StateFragmentTestActivityPresenter.Companion.TEST_ACTIVITY_PROFILE_ID_EXTRA_KEY
import org.oppia.android.app.player.state.testing.StateFragmentTestActivityPresenter.Companion.TEST_ACTIVITY_SHOULD_SAVE_PARTIAL_PROGRESS_EXTRA_KEY
import org.oppia.android.app.player.state.testing.StateFragmentTestActivityPresenter.Companion.TEST_ACTIVITY_STORY_ID_EXTRA_KEY
import org.oppia.android.app.player.state.testing.StateFragmentTestActivityPresenter.Companion.TEST_ACTIVITY_TOPIC_ID_EXTRA_KEY
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionWithSavedProgressListener
import javax.inject.Inject

private const val TAG_HINTS_AND_SOLUTION_DIALOG = "HINTS_AND_SOLUTION_DIALOG"

/** Test Activity used for testing StateFragment */
class StateFragmentTestActivity :
  InjectableAppCompatActivity(),
  StopStatePlayingSessionWithSavedProgressListener,
  StateKeyboardButtonListener,
  AudioButtonListener,
  HintsAndSolutionListener,
  RouteToHintsAndSolutionListener,
  RevealHintListener,
  RevealSolutionInterface,
  HintsAndSolutionExplorationManagerListener {
  @Inject
  lateinit var stateFragmentTestActivityPresenter: StateFragmentTestActivityPresenter
  private lateinit var state: State
  private lateinit var writtenTranslationContext: WrittenTranslationContext
  private lateinit var profileId: ProfileId

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    profileId = ProfileId.newBuilder().apply {
      internalId = intent.getIntExtra(TEST_ACTIVITY_PROFILE_ID_EXTRA_KEY, -1)
    }.build()
    stateFragmentTestActivityPresenter.handleOnCreate()
  }

  override fun deleteCurrentProgressAndStopSession(isCompletion: Boolean) {
    stateFragmentTestActivityPresenter.deleteCurrentProgressAndStopExploration(isCompletion)
  }

  override fun deleteOldestProgressAndStopSession() {}

  override fun onEditorAction(actionCode: Int) {}

  companion object {
    /**
     * Creates an [Intent] for opening new instances of [StateFragmentTestActivity].
     *
     * @param context the [Context] in which the activity should be opened
     * @param profileId the ID of the profile whose PIN needs to be verified
     * @param topicId the ID of the topic whose story chapter is being opened
     * @param storyId the ID of the story whose chapter is being opened
     * @param explorationId the ID of exploration being opened
     * @param shouldSavePartialProgress whether partial progress should be saved during the session
     * @return the new [Intent] that cna be used to open a [StateFragmentTestActivity]
     */
    fun createIntent(
      context: Context,
      profileId: Int,
      topicId: String,
      storyId: String,
      explorationId: String,
      shouldSavePartialProgress: Boolean
    ): Intent {
      val intent = Intent(context, StateFragmentTestActivity::class.java)
      intent.putExtra(TEST_ACTIVITY_PROFILE_ID_EXTRA_KEY, profileId)
      intent.putExtra(TEST_ACTIVITY_TOPIC_ID_EXTRA_KEY, topicId)
      intent.putExtra(TEST_ACTIVITY_STORY_ID_EXTRA_KEY, storyId)
      intent.putExtra(TEST_ACTIVITY_EXPLORATION_ID_EXTRA_KEY, explorationId)
      intent.putExtra(
        TEST_ACTIVITY_SHOULD_SAVE_PARTIAL_PROGRESS_EXTRA_KEY,
        shouldSavePartialProgress
      )
      return intent
    }
  }

  override fun showAudioButton() {}

  override fun hideAudioButton() {}

  override fun showAudioStreamingOn() {}

  override fun showAudioStreamingOff() {}

  override fun setAudioBarVisibility(isVisible: Boolean) {}

  override fun scrollToTop() {
    stateFragmentTestActivityPresenter.scrollToTop()
  }

  fun stopExploration(isCompletion: Boolean) {
    stateFragmentTestActivityPresenter.stopExploration(isCompletion)
  }

  override fun dismiss() {}

  override fun routeToHintsAndSolution(id: String, helpIndex: HelpIndex) {
    if (getHintsAndSolution() == null) {
      val hintsAndSolutionFragment =
        HintsAndSolutionDialogFragment.newInstance(
          id,
          state,
          helpIndex,
          writtenTranslationContext,
          profileId
        )
      hintsAndSolutionFragment.showNow(supportFragmentManager, TAG_HINTS_AND_SOLUTION_DIALOG)
    }
  }

  override fun revealHint(hintIndex: Int) {
    stateFragmentTestActivityPresenter.revealHint(hintIndex)
  }

  override fun revealSolution() {
    stateFragmentTestActivityPresenter.revealSolution()
  }

  override fun onExplorationStateLoaded(
    state: State,
    writtenTranslationContext: WrittenTranslationContext
  ) {
    this.state = state
    this.writtenTranslationContext = writtenTranslationContext
  }

  private fun getHintsAndSolution(): HintsAndSolutionDialogFragment? {
    return supportFragmentManager.findFragmentByTag(
      TAG_HINTS_AND_SOLUTION_DIALOG
    ) as HintsAndSolutionDialogFragment?
  }

  /** Dagger injector for [StateFragmentTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: StateFragmentTestActivity)
  }
}
