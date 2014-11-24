package io.leopard.web.mvc;

import io.leopard.monitor.url.UrlInfoService;
import io.leopard.topnb.PerformanceStackTraceService;
import io.leopard.web.annotation.NoXss;
import io.leopard.web.annotation.SkipFilter;
import io.leopard.web.security.xss.XssAttributeData;
import io.leopard.web.userinfo.service.SkipFilterService;
import io.leopard.web4j.captcha.CaptchaGroup;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class LeopardHandlerMapping extends RequestMappingHandlerMapping {
	@Autowired
	private SkipFilterService skipFilterService;

	/**
	 * 验证码分组
	 */
	@Override
	protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
		HandlerMethod handlerMethod = super.getHandlerInternal(request);
		if (handlerMethod != null) {
			// TODO ahai 每个URL都要做判断是否太浪费？如何解决？
			CaptchaGroup captchaGroup = handlerMethod.getMethod().getAnnotation(CaptchaGroup.class);
			if (captchaGroup != null) {
				String groupId = captchaGroup.value();
				request.setAttribute("captchaGroupId", groupId);
			}
		}
		return handlerMethod;
	}

	private final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	@Override
	protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
		super.registerHandlerMethod(handler, method, mapping);
		HandlerMethod handlerMethod = this.getHandlerMethods().get(mapping);

		// 按接口监控方法耗时配置
		String uri = mapping.getPatternsCondition().getPatterns().iterator().next();
		uri = uri.replaceAll("/+", "/");

		if (this.hasMonitor(mapping, method, uri)) {
			PerformanceStackTraceService.add(uri);
		}

		{
			// @SkipFilter
			SkipFilter skipFilter = handlerMethod.getMethodAnnotation(SkipFilter.class);
			if (skipFilter != null) {
				skipFilterService.add(uri);
			}
		}

		UrlInfoService.add(uri);

		MethodParameter[] params = handlerMethod.getMethodParameters();
		for (MethodParameter param : params) {
			NoXss noXss = param.getParameterAnnotation(NoXss.class);
			if (noXss == null) {
				continue;
			}
			param.initParameterNameDiscovery(parameterNameDiscoverer);
			String paramName = param.getParameterName();

			XssAttributeData.add(uri, paramName);
		}
	}

	// protected void checkRequestMapping(String uri, Method method) {
	// Class<?> clazz = method.getDeclaringClass();
	// RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
	// if (requestMapping != null) {
	// String[] values = requestMapping.value();
	// String value = values[0];
	// if (value.endsWith("/")) {
	// throw new IllegalArgumentException(clazz.getName() + " 的@RequestMapping.value不能以/结尾.");
	// }
	// }
	// // String message = clazz.getName() + "." + method.getName();
	// throw new IllegalArgumentException("uri不符合规则[" + uri + "]clazz:" + clazz.getName());
	// }

	@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		RequestMappingInfo info = null;
		RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		if (methodAnnotation != null) {
			RequestCondition<?> methodCondition = getCustomMethodCondition(method);
			info = createRequestMappingInfo(methodAnnotation, methodCondition, method);
			RequestMapping typeAnnotation = AnnotationUtils.findAnnotation(handlerType, RequestMapping.class);
			if (typeAnnotation != null) {
				RequestCondition<?> typeCondition = getCustomTypeCondition(handlerType);
				info = createRequestMappingInfo(typeAnnotation, typeCondition, null).combine(info);
			}
		}
		return info;
	}

	/**
	 * 判断接口是否需要监控.
	 * 
	 * @param info
	 * @param method
	 * @return
	 */
	protected boolean hasMonitor(RequestMappingInfo info, Method method, String uri) {
		String className = method.getDeclaringClass().getName();
		if (className.startsWith("io.leopard")) {
			return false;
		}
		// String uri = info.getPatternsCondition().getPatterns().iterator().next();
		if (uri.startsWith("/admin/")) {
			return false;
		}
		if (uri.startsWith("/monitor/")) {
			return false;
		}
		return true;
	}

	/**
	 * Created a RequestMappingInfo from a RequestMapping annotation.
	 */
	protected RequestMappingInfo createRequestMappingInfo(RequestMapping annotation, RequestCondition<?> customCondition, Method method) {
		String[] patterns;
		if (method != null && annotation.value().length == 0) {
			patterns = new String[] { this.createPattern(method.getName()) };
		}
		else {
			patterns = resolveEmbeddedValuesInPatterns(annotation.value());
		}
		return new RequestMappingInfo(new PatternsRequestCondition(patterns, getUrlPathHelper(), getPathMatcher(), false, this.useTrailingSlashMatch(), this.getFileExtensions()),
				new RequestMethodsRequestCondition(annotation.method()), new ParamsRequestCondition(annotation.params()), new HeadersRequestCondition(annotation.headers()),
				new ConsumesRequestCondition(annotation.consumes(), annotation.headers()), new ProducesRequestCondition(annotation.produces(), annotation.headers(), getContentNegotiationManager()),
				customCondition);
	}

	protected String createPattern(String methodName) {
		return methodName + ".do";
	}
}