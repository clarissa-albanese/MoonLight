// == Define locations for build logic ==
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("../build-logic")
}

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// == Define the inner structure of this component ==
rootProject.name = "examples" // the component name
// Spatio-temporal examples:
include("spatio-temporal:bikes")
include("spatio-temporal:city")
include("spatio-temporal:epidemic")
include("spatio-temporal:patterns")
include("spatio-temporal:sensors")
include("spatio-temporal:simpleGrid")
include("spatio-temporal:subway")
// temporal examples:
// TODO: add these
include ("temporal:amt")
include ("temporal:matlab")
include ("temporal:simpleTemporal")