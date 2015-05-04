package org.kyrin.koala.datasource;

import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * ***********************
 * 
 *  动态数据源
 *
 * ***********************
 * @author kyrin kyrincloud@qq.com 
 *
 * @date [2015年5月2日]
 *
 */
public class DynamicDataSource extends AbstractRoutingDataSource{

	private static Logger logger=Logger.getLogger(DynamicDataSource.class);
	
	private Map<Object,Object> targetDataSources;
	private static final String DEFAULT_TARGET_DATASOURCE="defaultTargetDataSource";
	private static final ThreadLocal<Stack<String>> threadLocal=new ThreadLocal<Stack<String>>(){
		@Override
		protected Stack<String> initialValue() {
			return new Stack<String>();
		}
	};
	
	@Override
	public void afterPropertiesSet() {
		if(targetDataSources==null){
			logger.error("The dataSources property is null ");
			throw new IllegalArgumentException("dataSources is null");
		}
		if(targetDataSources.get(DEFAULT_TARGET_DATASOURCE)==null){
			logger.error("Not set the key of 'defaultTargetDataSource' ");
			throw new IllegalArgumentException("Please set the key 'defaultTargetDataSource'");
		}
		super.setTargetDataSources(targetDataSources);
		super.setDefaultTargetDataSource(targetDataSources.get(DEFAULT_TARGET_DATASOURCE));
		super.afterPropertiesSet();
	}
	
	public static void changeFor(String target){
		logger.info("Change current dataSource to "+target);
		Stack<String> current=threadLocal.get();
		current.push(target);
	}
	
	@Override
	protected Object determineCurrentLookupKey() {
		Stack<String> current=threadLocal.get();
		if(current.isEmpty()) return "";
		String name=current.pop() ;
		return name== null ? "" : name;
	}
	
	public Map<Object, Object> getTargetDataSources() {
		return targetDataSources;
	}

	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		this.targetDataSources = targetDataSources;
	}

}
