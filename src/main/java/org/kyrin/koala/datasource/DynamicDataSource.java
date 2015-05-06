package org.kyrin.koala.datasource;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class DynamicDataSource extends AbstractRoutingDataSource implements ApplicationContextAware{

	private static Logger logger=Logger.getLogger(DynamicDataSource.class);
	
	private static ApplicationContext applicationContext;
	
	private Map<Object,Object> targetDataSources;
	private Map<Object,List<String>> dataSourceKeysGroup;
	private static final String DEFAULT_TARGET_DATASOURCE="defaultTargetDataSource";
	private static final ThreadLocal<Stack<String>> threadLocal=new ThreadLocal<Stack<String>>(){
		@Override
		protected Stack<String> initialValue() {
			return new Stack<String>();
		}
	};
	
	@SuppressWarnings("static-access")
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext=applicationContext;
	}
	
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
	
	public static void changeTo(String target){
		logger.info("Change current dataSource to "+target);
		Stack<String> current=threadLocal.get();
		current.push(target);
	}
	
	public  void changeToByGroup(String groupName){
		
		if(dataSourceKeysGroup==null){
			throw new IllegalArgumentException("Not set the property 'dataSourceKeysGroup'");
		}
		
		List<String> dataSourceKeys=dataSourceKeysGroup.get(groupName);
		if(dataSourceKeys==null || dataSourceKeys.size()==0){
			throw new IllegalArgumentException("Not set the groupName "+groupName);
		}
		Stack<String> current=threadLocal.get();
		String key=dataSourceKeys.get(0);
		if(!targetDataSources.containsKey(key)){
			logger.error(" The dataSource key "+key+" is wrong");
			throw new IllegalArgumentException("Error dataSource key "+key);
		}
		current.push(key);
		logger.info("Change current dataSource to "+key+" by dataSourceKeyGroup");
		reordering(dataSourceKeys);
	}
	
	private   void reordering(List<String> dataSourceKeys){
		//首尾调换位置，实现队列轮训的形式
		String first=dataSourceKeys.get(0);
		dataSourceKeys.remove(0);
		dataSourceKeys.add(first);
	}
	
	@Override
	protected Object determineCurrentLookupKey() {
		Stack<String> current=threadLocal.get();
		if(current.isEmpty()) return "";
		String name=current.pop() ;
		return name== null ? "" : name;
	}

	public static DynamicDataSource getInstance(){
		return (DynamicDataSource) applicationContext.getBean(DynamicDataSource.class);
	}
	
	public Map<Object, Object> getTargetDataSources() {
		return targetDataSources;
	}

	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		this.targetDataSources = targetDataSources;
	}
	
	public Map<Object, List<String>> getDataSourceKeysGroup() {
		return dataSourceKeysGroup;
	}

	public void setDataSourceKeysGroup(Map<Object, List<String>> dataSourceKeysGroup) {
		this.dataSourceKeysGroup = dataSourceKeysGroup;
	}
}
