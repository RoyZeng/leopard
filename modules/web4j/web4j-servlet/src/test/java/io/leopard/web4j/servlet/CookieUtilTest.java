package io.leopard.web4j.servlet;

import javax.servlet.http.Cookie;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class CookieUtilTest {

	@Test
	public void setCookie() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		CookieUtil.setCookie("name", "value", request, response);

		Assert.assertEquals("value", response.getCookie("name").getValue());
	}

	@Test
	public void setCookie2() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		CookieUtil.setCookie("name", "value", 10000, true, request, response);

		Assert.assertTrue(response.getCookie("name").isHttpOnly());
	}

	@Test
	public void getCookie() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		Assert.assertNull(CookieUtil.getCookie("name", request));

		Cookie cookie = new Cookie("name", "value");
		request.setCookies(cookie);
		Assert.assertEquals("value", CookieUtil.getCookie("name", request));

		Assert.assertNull(CookieUtil.getCookie("name1", request));
	}

	// @Test
	// public void getYygVersion() {
	// Assert.assertEquals(-1f, CookieUtil.getYygVersion(""), 0);
	//
	// MockHttpServletRequest request = new MockHttpServletRequest();
	//
	// Cookie cookie = new Cookie("yygVer", "(2.0.1.4)");
	// request.setCookies(cookie);
	// Assert.assertEquals(2.0f, CookieUtil.getYygVersion(request), 0);
	// }

	@Test
	public void deleteCookie() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		CookieUtil.deleteCookie("name", request, response);
		CookieUtil.deleteCookie("name", "leopard.io", response);

		Cookie cookie = response.getCookie("name");
		Assert.assertNotNull(cookie);
		Assert.assertEquals(-1, cookie.getMaxAge());
		System.out.println("cookie:" + cookie);

	}

	@Test
	public void CookieUtil() {
		new CookieUtil();
	}

}