/**
 * Copyright &copy; 2015-2020 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.citic.common.web;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.alibaba.fastjson.JSONObject;
import com.citic.common.mapper.JsonMapper;
import com.citic.common.utils.DateUtils;

/**
 * 控制器支持类
 * @author jeeplus
 * @version 2013-3-23
 */
public abstract class BaseController {

	/**
	 * 日志对象
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 管理基础路径
	 */
	@Value("${adminPath}")
	protected String adminPath;
	
	/**
	 * 前端基础路径
	 */
	@Value("${frontPath}")
	protected String frontPath;
	
	/**
	 * 前端URL后缀
	 */
	@Value("${urlSuffix}")
	protected String urlSuffix;
	

	
	/**
	 * 全局参数验证异常处理
	 * @param red
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value=Exception.class)
	@ResponseBody
	JSONObject defaultErrorHandler(HttpServletRequest red,Exception e){
		JSONObject data=new JSONObject();
		String msg="服务器繁忙，稍后重试！";
		int code=500;
		if(e instanceof NoHandlerFoundException){
			msg="页面不存在！";
			code=404;
		}else  if(e instanceof AuthenticationException){
			msg="无权限访问！";
			code=403;
		}else  if(e instanceof MissingServletRequestParameterException)
		{
			msg="缺少参数"+((MissingServletRequestParameterException)e).getParameterName();
			code=100;
		} else if(e instanceof MethodArgumentTypeMismatchException)
		{
			code=101;
			msg=((MethodArgumentTypeMismatchException)e).getName()+
					"参数类型不合法，需要类型为"+
					((MethodArgumentTypeMismatchException)e).getRequiredType().getCanonicalName();
		}
		data.put("msg", msg);
		data.put("code", code);
		logger.error("异常：",e);
		return data;
	}
	
	

	
	/**
	 * 客户端返回JSON字符串
	 * @param response
	 * @param object
	 * @return
	 */
	protected String renderString(HttpServletResponse response, Object object) {
		return renderString(response, JsonMapper.toJsonString(object));
	}
	
	/**
	 * 客户端返回字符串
	 * @param response
	 * @param string
	 * @return
	 */
	protected String renderString(HttpServletResponse response, String string) {
		try {
			response.reset();
	        response.setContentType("application/json");
	        response.setCharacterEncoding("utf-8");
			response.getWriter().print(string);
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * 
	 * @param code  
	 *   错误代号
	  	000：操作成功
		001：操作失败
		002：操作异常
		003：会话已失效
		004：权限不足
		005：数据格式错误
		998：{errmsg}
		999：无意义数据
		101：帐号或密码为空
		102：未授权登录
		103：账号或密码错误
		104：密码错误
		105：新密码和旧密码一样
		106：密码为空
		201：用户不存在
		203：手机号已被使用
		204：用户姓名不能为空
		205：用户手机号不能为空
		310：应用不存在
		310：场景不存在
		310：场景已经存在
		320：词库不存在
		601：权限名不能为空
		602：权限名已存在
		603：权限不存在
	 * @param data 返回数据
	 * @return
	 */
	protected JSONObject responseBody(Integer code,Object data) {
		JSONObject res=new JSONObject();
		res.put("code", code);
		res.put("msg", ResultCode.getMsg(code)+"!");
		res.put("data", data);
		return res;
	}
	
	 
	
	/**
	 * 初始化数据绑定
	 * 1. 将所有传递进来的String进行HTML编码，防止XSS攻击
	 * 2. 将字段中Date类型转换为String类型
	 */
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		// String类型转换，将所有传递进来的String进行HTML编码，防止XSS攻击
	    // 设置接受数据list的大小
	    binder.setAutoGrowNestedPaths(true);  
        binder.setAutoGrowCollectionLimit(1024*10);  
		binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) {
				setValue(text == null ? null : StringEscapeUtils.escapeHtml4(text.trim()));
			}
			@Override
			public String getAsText() {
				Object value = getValue();
				return value != null ? value.toString() : "";
			}
		});
		// Date 类型转换
		binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) {
				setValue(DateUtils.parseDate(text));
			}
//			@Override
//			public String getAsText() {
//				Object value = getValue();
//				return value != null ? DateUtils.formatDateTime((Date)value) : "";
//			}
		});
	}
	
}
