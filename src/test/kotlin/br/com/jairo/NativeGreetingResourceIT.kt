package br.com.jairo

import io.quarkus.test.junit.NativeImageTest

@NativeImageTest
open class NativeGreetingResourceIT : GreetingResourceTest()