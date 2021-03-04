package com.example.demo

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.sql.ResultSet

@RestController
class Controller(
    val repository: FooRepository,
    val jdbcTemplate: NamedParameterJdbcTemplate,
    val tenantRepository: TenantRepository
) {


    @GetMapping("/data-jdbc/{tenantId}/foo/{id}")
    fun getFooViaDataJdbc(@PathVariable("tenantId") tenantId: Int, @PathVariable("id") id: Int): Foo {
        Thread.sleep(10)
        val tenant = tenantRepository.findById(tenantId);
        return repository.findById(id).get()
    }

    @GetMapping("/jdbc-template/{tenantId}/foo/{id}")
    fun getFooViaJdbcTemplate(@PathVariable("tenantId") tenantId: Int, @PathVariable("id") id: Int): Foo {
        Thread.sleep(10)
        val tenant = tenantRepository.findById(tenantId);
        return jdbcTemplate.queryForObject(
            "select * from FOO where ID = :id",
            mapOf(Pair("id", id))
        ) { resultSet: ResultSet, _: Int ->
            Foo(resultSet.getInt(1), resultSet.getString(2))
        }!!
    }

}