package br.com.jairo.repository

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import org.eclipse.microprofile.config.ConfigProvider
import java.io.FileInputStream
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
abstract class AbstractRepository {

  private final val credentialsPath: String = ConfigProvider.getConfig().getValue("google.cloud.credentials.path", String::class.java)

  final val store: Datastore

  constructor() {
    store = try {
      val inputStream = FileInputStream(credentialsPath)
      val googleCredentials = GoogleCredentials.fromStream(inputStream)
        .createScoped(arrayListOf("https://www.googleapis.com/auth/cloud-platform"))
      DatastoreOptions.newBuilder()
        .setCredentials(googleCredentials)
        .build().service
    } catch (ex: Exception) {
      println("Get Google Engine Credentials from Compute Engine")
      DatastoreOptions.getDefaultInstance().service
    }
  }


}