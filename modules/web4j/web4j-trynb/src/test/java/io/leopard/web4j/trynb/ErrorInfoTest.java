package io.leopard.web4j.trynb;

import io.leopard.test4j.mock.BeanAssert;
import io.leopard.web4j.trynb.model.ErrorInfo;

import org.junit.Test;

public class ErrorInfoTest {

	@Test
	public void ErrorInfo() {
		BeanAssert.assertModel(ErrorInfo.class);
	}

}