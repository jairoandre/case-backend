package br.com.jairo.resource

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/greeting")
class GreetingResource {
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  fun greeting(): String {
    return "hello"
  }
}