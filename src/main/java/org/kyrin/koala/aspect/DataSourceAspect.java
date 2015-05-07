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
import org.kyrin.koala.annotation.DataSourceGroup;
import org.kyrin.koala.annotation.Group;
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
	
	@Pointcut(value="@within(org.kyrin.koala.annotation.DataSourceGroup)")
	public void inWebServiceClass2(){}
	
	@Pointcut(value="inWebServiceClass() || inWebServiceClass2()")
	public void all(){}
	
	//添加synchronized关键字，避免在同一个group下的数据源轮训不均匀
	@Before(value="all()")
	public synchronized void classBefore(JoinPoint jp){
		MethodSignature sig=(MethodSignature) jp.getSignature();
		Method method=sig.getMethod();
		String name=method.getName();
		Annotation annotation=method.getAnnotation(ChangeTo.class);
		if(annotation!=null){
			return;
		}
		Object obj=jp.getTarget();
		DataSourceDistribute dataSourceDistribute=obj.getClass().getAnnotation(DataSourceDistribute.class);
		DataSourceGroup dataSourceGroup=obj.getClass().getAnnotation(DataSourceGroup.class);
		if(dataSourceDistribute!=null){
			if(dealDataSourceDistribute(dataSourceDistribute,name)){
				return;
			}
		}
		if(dataSourceGroup!=null){
			dealDataSourceGroup(dataSourceGroup,name);
		}
	}
	
	private  boolean dealDataSourceDistribute(DataSourceDistribute dataSourceDistribute,String methodName){
		DataSourceEntity [] entity=dataSourceDistribute.value();
		if(entity!=null){
			for(DataSourceEntity en:entity){
				if(Pattern.matches(en.methodPattern(), methodName)){
					DynamicDataSource.changeTo(en.dataSource());
					return true;
				}
			}
		}
		return false;
	}
	
	private  boolean dealDataSourceGroup(DataSourceGroup dataSourceGroup,String methodName){
		Group[] groups=dataSourceGroup.groups();
		if(groups!=null){
			for(Group group:groups){
				if(Pattern.matches(group.methodPattern(), methodName)){
					DynamicDataSource.getInstance().changeToByGroup(group.groupName());
					return true;
				}
			}
		}	
		return false;
	}
	
	@Around(value="inWebSevice()")
	public void around(ProceedingJoinPoint pj) throws Throwable{
		logger.info("The Around has been start !");
		MethodSignature sig=(MethodSignature) pj.getSignature();
		Method method=sig.getMethod();
		Annotation annotation=method.getAnnotation(ChangeTo.class);
		if(annotation!=null){
			ChangeTo changeTo=(ChangeTo)annotation;
			DynamicDataSource.changeTo(changeTo.value());
			logger.info("Success change dataSource to ："+changeTo.value());
		}
		pj.proceed();
		logger.info("The Around has been stop !");
	}
}
