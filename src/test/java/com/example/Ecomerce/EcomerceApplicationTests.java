package com.example.Ecomerce;

import com.example.Ecomerce.config.FlywayRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EcomerceApplicationTests {

	@BeforeAll
	static void migrateSchema() {
		// @SpringBootTest boots the context directly (bypassing EcomerceApplication.main()),
		// so migrations have to be triggered explicitly here too.
		FlywayRunner.migrate();
	}

	@Test
	void contextLoads() {
	}

}
