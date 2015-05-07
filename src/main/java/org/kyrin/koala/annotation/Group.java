package org.kyrin.koala.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Group {
	
	/*
	 * 已经分好组的数据源组名
	 */
	public String groupName() default "";
	
	/*
	 * 设置方法名，以正则表达式的形式可以匹配多个
	 */
	public String methodPattern() default "";

}
