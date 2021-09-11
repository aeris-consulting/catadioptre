package io.aerisconsulting.catadioptre.example;

import io.aerisconsulting.catadioptre.Testable;

class DefaultCatadioptreExample {

	@Testable
	private String toLowerCase(String text) {
		return text.toLowerCase();
	}

	@Testable
	private String callMethodThrowingException(String example) {
		throw new IllegalStateException(example);
	}
}
