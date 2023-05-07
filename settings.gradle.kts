rootProject.name = "TelegramPoll"

pluginManagement {
    // Read from ~/.gradle/gradle.properties
    val githubUsername: String by settings
    val githubToken: String by settings

    repositories {
        gradlePluginPortal()
        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/arenacraft/gradle-conventions")
            credentials {
                username = githubUsername
                password = githubToken
            }
        }
    }
}
