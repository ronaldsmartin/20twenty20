package com.itsronald.twenty2020.feedback

import android.os.Bundle
import com.heinrichreimersoftware.androidissuereporter.IssueReporterActivity
import com.heinrichreimersoftware.androidissuereporter.model.github.GithubTarget
import com.itsronald.twenty2020.BuildConfig

/**
 * Allows user-submitted feedback to be created as GitHub issues on our repo.
 */
class FeedbackActivity : IssueReporterActivity() {

    companion object {
        const val TARGET_USERNAME = "ronaldsmartin"
        const val TARGET_REPOSITORY = "20twenty20"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setGuestEmailRequired(true)
    }

    override fun getTarget(): GithubTarget = GithubTarget(TARGET_USERNAME, TARGET_REPOSITORY)

    override fun getGuestToken(): String = BuildConfig.GITHUB_ISSUE_BOT_API_KEY
}