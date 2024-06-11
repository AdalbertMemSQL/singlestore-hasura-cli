pluginManagement {

    plugins {
        kotlin("jvm") version "1.9.22"
    }
}

rootProject.name = "singlestore-hasura-connector"

include(":ndc-ir")
include(":ndc-cli-singlestore")
