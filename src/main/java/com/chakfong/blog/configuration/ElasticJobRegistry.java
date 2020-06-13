//package com.chakfong.blog.configuration;
//
//import com.dangdang.ddframe.job.config.JobCoreConfiguration;
//import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
//import com.dangdang.ddframe.job.lite.api.JobScheduler;
//import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
//import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
//import com.chakfong.blog.annotation.ElasticJob;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.aop.framework.AopProxyUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationListener;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.context.event.ContextRefreshedEvent;
//import org.springframework.core.annotation.AnnotationUtils;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.atomic.AtomicBoolean;
//
//@Component
//@Slf4j
//public class ElasticJobRegistry implements ApplicationListener<ContextRefreshedEvent> {
//
//    final ZookeeperRegistryCenter zookeeperRegistryCenter;
//
//    private final AtomicBoolean initialized = new AtomicBoolean(false);
//
//    public ElasticJobRegistry(ZookeeperRegistryCenter zookeeperRegistryCenter) {
//        this.zookeeperRegistryCenter = zookeeperRegistryCenter;
//    }
//
//    @Override
//    public void onApplicationEvent(ContextRefreshedEvent event) {
//        if (zookeeperRegistryCenter != null && initialized.compareAndSet(false, true)) {
//            initialize((ConfigurableApplicationContext) event.getApplicationContext());
//        }
//    }
//
//    private void initialize(ConfigurableApplicationContext applicationContext) {
//        for (Object bean : applicationContext.getBeansWithAnnotation(ElasticJob.class).values()) {
//            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
//            ElasticJob annotation = AnnotationUtils.findAnnotation(targetClass, ElasticJob.class);
//
//            if (annotation != null) {
//                log.info("ElasticJob's cron: " + annotation.cron());
//                // 定义作业核心配置
//                JobCoreConfiguration simpleCoreConfig = JobCoreConfiguration.newBuilder("demo", annotation.cron(), 1).build();
//                // 定义SIMPLE类型配置
//                SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(simpleCoreConfig, targetClass.getCanonicalName());
//                // 定义Lite作业根配置
//                LiteJobConfiguration simpleJobRootConfig = LiteJobConfiguration.newBuilder(simpleJobConfig).build();
//                JobScheduler scheduler = new JobScheduler(zookeeperRegistryCenter, simpleJobRootConfig);
//
//                scheduler.init();
//                applicationContext.getBeanFactory().registerSingleton(targetClass.getCanonicalName() + "Scheduler", scheduler);
//            }
//        }
//    }
//}
