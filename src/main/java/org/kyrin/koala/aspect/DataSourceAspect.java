package org.kyrin.koala.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.kyrin.koala.annotation.ChangeTo;
import org.kyrin.koala.annotation.DataSourceDistribute;
import org.kyrin.koala.annotation.DataSourceEntity;
import org.kyrin.koala.datasource.DynamicDataSource;
import org.springframework.core.annotation.Order;


/**
 * ***********************
 * 
 *  dataSourceAspect
 *
 * ***********************
 * @author kyrin kyrincloud@qq.com 
 *
 * @date [2015年5月4日]
 *
 */
@Aspect
@Order(value=0)
public class DataSourceAspect {
	
	private static Logger logger=Logger.getLogger(DataSourceAspect.class);
	
	@Pointcut(value="@annotation(org.kyrin.koala.annotation.ChangeTo)")
	public void inWebSevice(){}
	
	@Pointcut(value="@within(org.kyrin.koala.annotation.DataSourceDistribute)")
	public void inWebServiceClass(){}
	
	@Before(value="inWebServiceClass()")
	public void classBefore(JoinPoint jp){
		MethodSignature sig=(MethodSignature) jp.getSignature();
		Method method=sig.getMethod();
		String name=method.getName();
		Annotation annotation=method.getAnnotation(ChangeTo.class);
		if(annotation!=null){
			return;
		}
		Object targetObj=jp.getTarget();
		Annotation[] anns=targetObj.getClass().getAnnotations();
		DataSourceEntity [] entity=null;
		for(Annotation an:anns){
			if(an instanceof DataSourceDistribute){
				DataSourceDistribute dsd=(DataSourceDistribute)an;
				entity=dsd.value();
				break;
			}
		}
		
		if(entity!=null){
			for(DataSourceEntity dse:entity){
				if(Pattern.matches(dse.method(), name)){
					DynamicDataSource.changeFor(dse.dataSource());
					return;
				}
			}
		}
	}
	
	@Around(value="inWebSevice()")
	public void around(ProceedingJoinPoint pj) throws Throwable{
		logger.info("The Around has been start !");
		MethodSignature sig=(MethodSignature) pj.getSignature();
		Method method=sig.getMethod();
		Annotation annotation=method.getAnnotation(ChangeTo.class);
		if(annotation!=null){
			ChangeTo changeFor=(ChangeTo)annotation;
			DynamicDataSource.changeFor(changeFor.value());
			logger.info("Success change dataSource to ："+changeFor.value());
		}
		pj.proceed();
		logger.info("The Around has been stop !");
	}
}