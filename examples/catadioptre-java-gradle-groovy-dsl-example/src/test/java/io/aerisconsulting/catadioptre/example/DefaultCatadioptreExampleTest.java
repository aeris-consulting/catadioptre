package io.aerisconsulting.catadioptre.example;

import io.aerisconsulting.catadioptre.CatadioptreOriginalCauseException;
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

	@Test
	@DisplayName("should throw the same exception that the real method")
	void shouldThrowSameExceptionRealMethod() {
		DefaultCatadioptreExample instance = new DefaultCatadioptreExample();

		Assertions.assertThatExceptionOfType(CatadioptreOriginalCauseException.class).isThrownBy( () -> {
			TestableDefaultCatadioptreExample.callMethodThrowingException(instance, "test");
		}).withCauseInstanceOf(IllegalStateException.class);
	}
}
