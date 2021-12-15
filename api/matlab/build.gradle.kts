plugins {
    id("java-library")
}

group = "${group}.api"


dependencies {
    implementation("eu.quanticol.moonlight.core:monitor-core")
    implementation("eu.quanticol.moonlight.core:utility")
    implementation("eu.quanticol.moonlight.script:parser")
}
//tasks.jar {
//    archiveFileName= "moonlightAPI.jar"
//}