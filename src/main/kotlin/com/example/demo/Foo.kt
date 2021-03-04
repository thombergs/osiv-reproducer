package com.example.demo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("FOO")
data class Foo(
    @Id
    val id: Int,
    val name: String
)