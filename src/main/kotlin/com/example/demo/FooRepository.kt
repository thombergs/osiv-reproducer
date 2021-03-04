package com.example.demo

import org.springframework.data.repository.CrudRepository

interface FooRepository : CrudRepository<Foo, Int> {
}