package org.kyrin.koala.config;

import org.kyrin.koala.aspect.DataSourceAspect;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


public class KoalaAspectBeanDefinitionParser implements BeanDefinitionParser{

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		RootBeanDefinition root=new RootBeanDefinition();
		root.setBeanClass(DataSourceAspect.class);
		parserContext.getRegistry().registerBeanDefinition(DataSourceAspect.class.getName(),root);
		return root;
	}
}
