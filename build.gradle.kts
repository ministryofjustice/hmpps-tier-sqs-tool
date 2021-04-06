
plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.1.3"
  kotlin("plugin.spring") version "1.4.30"
  kotlin("plugin.jpa") version "1.4.30"
  id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework:spring-webflux")
  implementation("org.springframework.boot:spring-boot-starter-reactor-netty")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")

  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.0.8")
  implementation("com.opencsv:opencsv:5.2")
  implementation("com.google.code.gson:gson:2.8.6")

  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation("org.awaitility:awaitility-kotlin:4.0.3")
}

tasks.register("fix") {
  dependsOn(":ktlintFormat")
}
