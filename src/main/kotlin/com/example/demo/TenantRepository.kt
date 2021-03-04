package com.example.demo

import org.springframework.data.repository.CrudRepository

interface TenantRepository : CrudRepository<Tenant, Int> {
}