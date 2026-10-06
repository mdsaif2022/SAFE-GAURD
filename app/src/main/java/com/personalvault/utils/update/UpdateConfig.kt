package com.personalvault.utils.update

/**
 * Official GitHub repository configuration for PersonalVault releases and update checking.
 * Replace [GITHUB_OWNER] with your actual GitHub username or organization name.
 */
object UpdateConfig {
    const val GITHUB_OWNER = "mdsaif2022"
    const val GITHUB_REPO = "SAFE GAURD"

    val RELEASES_API_URL: String
        get() = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    const val CHECK_INTERVAL_HOURS = 24L
    const val NOTIFICATION_CHANNEL_ID = "personal_vault_updates"
    const val NOTIFICATION_ID = 1001
}
