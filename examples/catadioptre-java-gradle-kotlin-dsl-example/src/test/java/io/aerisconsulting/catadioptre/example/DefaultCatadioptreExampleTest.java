package io.aerisconsulting.catadioptre.example;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultCatadioptreExampleTest {

	@Test
	@DisplayName("should execute the private method in class with default visibility")
	void shouldExecuteThePrivateToLowerCaseFunction() {
		DefaultCatadioptreExample instance = new DefaultCatadioptreExample();
		String result = TestableDefaultCatadioptreExample.toLowerCase(instance, "T");
		Assertions.assertThat(result).isEqualTo("t");
	}
}
