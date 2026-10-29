import java.util.Base64

plugins {
  alias(libs.plugins.android.application) apply false
}

val debugKeystoreFile = file("${rootDir}/debug.keystore")
val debugKeystoreBase64File = file("${rootDir}/debug.keystore.base64")
if (!debugKeystoreFile.exists() && debugKeystoreBase64File.exists()) {
  val cleanedBase64 = debugKeystoreBase64File.readText().replace("\\s".toRegex(), "")
  debugKeystoreFile.writeBytes(Base64.getDecoder().decode(cleanedBase64))
}
