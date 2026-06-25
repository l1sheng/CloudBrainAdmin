package com.neuCloudBrainMedical.admin.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

	@Test
	void generateTokenCanBeValidatedAndParsed() {
		JwtTokenProvider provider = new JwtTokenProvider();
		ReflectionTestUtils.setField(provider, "secret",
				"doctor-platform-development-secret-key-with-at-least-32-bytes");
		ReflectionTestUtils.setField(provider, "expiration", 86400000L);

		String token = provider.generateToken(7L, "admin", "ADMIN");

		assertThat(provider.validateToken(token)).isTrue();
		assertThat(provider.getUserIdFromToken(token)).isEqualTo(7L);
		assertThat(provider.getUsernameFromToken(token)).isEqualTo("admin");
	}

	@Test
	void validateTokenReturnsFalseForInvalidToken() {
		JwtTokenProvider provider = new JwtTokenProvider();
		ReflectionTestUtils.setField(provider, "secret",
				"doctor-platform-development-secret-key-with-at-least-32-bytes");
		ReflectionTestUtils.setField(provider, "expiration", 86400000L);

		assertThat(provider.validateToken("invalid.token.value")).isFalse();
	}
}
