plugins {
	java
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "io.student"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

val apacheCommonsCompressVersion = "1.26.0"
val apacheCommonsLangVersion = "3.18.0"
val commonsCodecVersion = "1.17.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.postgresql:postgresql")
	implementation("org.apache.commons:commons-lang3:${apacheCommonsLangVersion}")
	implementation("org.apache.commons:commons-compress:${apacheCommonsCompressVersion}")
	implementation("commons-codec:commons-codec:${commonsCodecVersion}")
	implementation("org.springframework.kafka:spring-kafka")
	implementation(libs.jjwt.api)

	runtimeOnly(libs.jjwt.impl)
	runtimeOnly(libs.jjwt.jackson)

	compileOnly("org.projectlombok:lombok")

	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:kafka")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
