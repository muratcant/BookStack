package org.muratcant.bookstack

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:postgresql://localhost:5433/bookstack_test",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
class BookStackIntegrationTest {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Test
    fun `context loads successfully`() {
        assertNotNull(applicationContext)
    }

    @Test
    fun `application has correct name`() {
        assertNotNull(applicationContext.applicationName)
    }
}
