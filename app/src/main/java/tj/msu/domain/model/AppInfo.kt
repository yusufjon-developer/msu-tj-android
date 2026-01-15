package tj.msu.domain.model

data class AppInfo(
    val latestVersion: String = "",
    val forceUpdate: Boolean = false,
    val url: String = "",
    val changelog: String = ""
)
