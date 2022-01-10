plugins {
    id("eu.quanticol.java-library")
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("application")
}

//version("unspecified")

dependencies {
    implementation("eu.quanticol.moonlight:core")
    implementation("eu.quanticol.moonlight:script")
    implementation("com.opencsv:opencsv:5.5.2")
    implementation("org.gillius:jfxutils:1.0")

    implementation("org.controlsfx:controlsfx:11.1.1")

    implementation("org.graphstream:gs-core:2.0")

    implementation("org.graphstream:gs-ui-javafx:2.0")

    implementation("org.graphstream:gs-algo:2.0")

    implementation("com.google.code.gson:gson:2.8.9")

    implementation("de.jensd:fontawesomefx:8.2")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")

    implementation("jgrapht:jgrapht:0.7.3")
    implementation("org.javabits.jgrapht:jgrapht-core:0.9.3")



}

javafx {
    version = "15"
    modules("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("App")
}