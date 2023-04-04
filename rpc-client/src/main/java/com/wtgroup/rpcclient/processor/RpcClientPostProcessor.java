package com.wtgroup.rpcclient.processor;

import com.wtgroup.rpcclient.annotation.RpcReference;
import com.wtgroup.rpcclient.domain.RpcReferenceBean;
import com.wtgroup.rpccore.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BeanFactoryPostProcessor在实例化bean对象之前可以对BeanDefinition进行修改
 */
@Component
@Slf4j
public class RpcClientPostProcessor implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor {

    // 容器对象
    private ApplicationContext applicationContext;
    // 类加载器对象
    private ClassLoader classLoader;

    private final Map<String, BeanDefinition> rpcRefBeanDefinitions = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            // 这里解释下，BeanDefinitionName并不一定等于BeanClassName，所以这里还有很有必要的
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                // 通过反射拿到Class对象
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                // ReflectionUtils为反射工具类，处理反射类的成员变量
                ReflectionUtils.doWithFields(clazz, this::parseRpcReference);
            }
        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        this.rpcRefBeanDefinitions.forEach((beanName, beanDefinition) -> {
            if (applicationContext.containsBean(beanName)) {
                throw new IllegalArgumentException("spring context already has a bean named " + beanName);
            }
            // 注册已生成的BeanDefinition
            registry.registerBeanDefinition(beanName, rpcRefBeanDefinitions.get(beanName));
            log.info("registered RpcReferenceBean {} success.", beanName);
        });
    }

    /**
     * 检测成员变量上是否有@RpcReference注解
     */
    private void parseRpcReference(Field field) {
        // 获取成员变量上的@RpcReference注解
        RpcReference annotation = AnnotationUtils.getAnnotation(field, RpcReference.class);
        if (annotation != null) {
            // 构建者模式，动态生成目标类的BeanDefinition
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcReferenceBean.class);
            // 定义bean的初始化方法
            builder.setInitMethodName(Constants.INIT_METHOD_NAME);
            // 初始化成员变量
            builder.addPropertyValue("interfaceClass", field.getType());
            builder.addPropertyValue("serviceVersion", annotation.serviceVersion());
            builder.addPropertyValue("registryType", annotation.registryType());
            builder.addPropertyValue("registryAddr", annotation.registryAddress());
            builder.addPropertyValue("timeout", annotation.timeout());
            BeanDefinition beanDefinition = builder.getBeanDefinition();
            // 注意这里只是生成了目标类的BeanDefinition，没有registry到容器中（即执行beanFactory.registerBeanDefinition(name, beanDefinition)），不会生成相应的bean
            rpcRefBeanDefinitions.put(field.getName(), beanDefinition);
        }
    }
}
