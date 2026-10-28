import java.util.Base64

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

// Automatically materialize debug.keystore from debug.keystore.base64 in CI environments (e.g., GitHub Actions)
val debugKeystoreFile = file("${rootDir}/debug.keystore")
val debugKeystoreBase64File = file("${rootDir}/debug.keystore.base64")
if (!debugKeystoreFile.exists() && debugKeystoreBase64File.exists()) {
  val cleanedBase64 = debugKeystoreBase64File.readText().replace("\\s".toRegex(), "")
  debugKeystoreFile.writeBytes(Base64.getDecoder().decode(cleanedBase64))
}
