package com.fiap.soat11.production;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProductionApplicationTests {

	@Test
	@Disabled("Desabilitado devido a dependências de AWS que não estão disponíveis em ambiente de teste")
	void contextLoads() {
	}

}
