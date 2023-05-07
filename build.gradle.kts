plugins {
    id("it.arenacraft.copy-conventions") version "1.0-SNAPSHOT"
    id("it.arenacraft.plugin-bridge-conventions") version "1.0-SNAPSHOT"
}

val hiddenShadow = createHiddenShadowConfiguration(true)

dependencies {

    implementation("org.telegram:telegrambots:6.1.0")
    hiddenShadow("org.telegram:telegrambots:6.1.0")

    implementation("org.telegram:telegrambotsextensions:6.1.0")
    hiddenShadow("org.telegram:telegrambotsextensions:6.1.0")

    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.18.0")
    hiddenShadow("org.apache.logging.log4j:log4j-slf4j-impl:2.18.0")

    implementation("at.favre.lib", "bcrypt","0.4.1")
    hiddenShadow("at.favre.lib", "bcrypt","0.4.1")

    implementation("it.arenacraft", "data-core-api", "3.0-SNAPSHOT")
    implementation("it.arenacraft", "mini-orm-api", "1.0-SNAPSHOT")
}
