package com.neuCloudBrainMedical.admin.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

	@Test
	void successWrapsDataWithDefaultCodeAndMessage() {
		Result<String> result = Result.success("ok");

		assertThat(result.getCode()).isEqualTo(200);
		assertThat(result.getMessage()).isEqualTo("success");
		assertThat(result.getData()).isEqualTo("ok");
	}

	@Test
	void errorWrapsCodeAndMessageWithNullData() {
		Result<Object> result = Result.error(400, "bad request");

		assertThat(result.getCode()).isEqualTo(400);
		assertThat(result.getMessage()).isEqualTo("bad request");
		assertThat(result.getData()).isNull();
	}
}
