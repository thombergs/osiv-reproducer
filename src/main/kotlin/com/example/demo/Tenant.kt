package com.example.demo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("TENANT")
class Tenant(
    @Id
    private val id: Long,
    private val name: String
)