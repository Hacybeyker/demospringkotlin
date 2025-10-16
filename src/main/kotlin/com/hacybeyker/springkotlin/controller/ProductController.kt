package com.hacybeyker.springkotlin.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController {

    @GetMapping("/product")
    fun getProducts(): Map<String, String> {
        return  mapOf("message" to "Hello World!")
    }

}