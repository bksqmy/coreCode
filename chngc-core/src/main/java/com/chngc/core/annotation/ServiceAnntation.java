package com.chngc.core.annotation;

import java.lang.annotation.*;


@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceAnntation {

	/**
	 * 注释
	 * @return
	 */
	public String note() default "暂无注释";
}
