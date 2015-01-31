package io.leopard.web4j.passport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通行证(用户)登录验证接口
 * 
 * @author 阿海
 *
 */
public interface PassportValidateDao {

	/**
	 * 获取当前登录的用户信息(Leopard会自动将返回值存入session作缓存).
	 * 
	 * @return 通行证或uid
	 */
	PassportUser validate(HttpServletRequest request, HttpServletResponse response);
}