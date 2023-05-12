package org.oppia.android.app.topic.revisioncard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.RevisionCardFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** Presenter for [RevisionCardFragment], sets up bindings from ViewModel. */
@FragmentScope
class RevisionCardFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @TopicHtmlParserEntityType private val entityType: String,
  private val translationController: TranslationController,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val revisionCardViewModelFactory: RevisionCardViewModel.Factory
) : HtmlParser.CustomOppiaTagActionListener {
  private lateinit var profileId: ProfileId

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    topicId: String,
    subtopicId: Int,
    profileId: ProfileId,
    subtopicListSize: Int
  ): View? {
    this.profileId = profileId

    val binding =
      RevisionCardFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    val view = binding.revisionCardExplanationText
    val viewModel = revisionCardViewModelFactory.create(
      topicId,
      subtopicId,
      profileId,
      subtopicListSize
    )

    logRevisionCardEvent(topicId, subtopicId)

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }

    viewModel.revisionCardLiveData.observe(
      fragment
    ) { ephemeralRevisionCard ->
      val pageContentsHtml =
        translationController.extractString(
          ephemeralRevisionCard.revisionCard.pageContents,
          ephemeralRevisionCard.writtenTranslationContext
        )
      view.text = htmlParserFactory.create(
        resourceBucketName, entityType, topicId, imageCenterAlign = true,
        customOppiaTagActionListener = this,
        displayLocale = appLanguageResourceHandler.getDisplayLocale()
      ).parseOppiaHtml(
        pageContentsHtml, view, supportsLinks = true, supportsConceptCards = true
      )
    }

    return binding.root
  }

  /** Dismisses the concept card fragment if it's currently active in this fragment. */
  fun dismissConceptCard() {
    ConceptCardFragment.dismissAll(fragment.childFragmentManager)
  }

  private fun logRevisionCardEvent(topicId: String, subTopicId: Int) {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenRevisionCardContext(topicId, subTopicId),
      profileId
    )
  }

  override fun onConceptCardLinkClicked(view: View, skillId: String) {
    ConceptCardFragment.bringToFrontOrCreateIfNew(skillId, profileId, fragment.childFragmentManager)
  }
}
