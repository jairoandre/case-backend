package br.com.jairo.resource.common

import org.jboss.resteasy.annotations.providers.multipart.PartType
import java.io.InputStream
import javax.ws.rs.FormParam
import javax.ws.rs.core.MediaType

data class MultipartBody(
  @field:FormParam("file")
  @field:PartType(MediaType.APPLICATION_OCTET_STREAM)
  val file: InputStream? = null,
  @field:FormParam("fileName")
  @field:PartType(MediaType.TEXT_PLAIN)
  val fileName: String? = null
  ) {
}