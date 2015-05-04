package org.kyrin.koala.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface DataSourceEntity{

	/**
	 * 指定对应的方法所使用的数据源，默认为defaultTargetDataSource
	 * @return
	 */
	String dataSource() default "defaultTargetDataSource";
	
	/**
	 * 	以正则表达式的形式来表示业务层的方法，
	 * 指定的这些方法统一使用dataSource作为数据源
	 * @return
	 */
	String method() default "";
}
