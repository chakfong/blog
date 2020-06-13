//package com.chakfong.blog.configuration;
//
//import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
//import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@ConditionalOnExpression("'${elastic.zookeeper.server-lists}'.length() >0")
//public class ElasticJobAutoConfiguration {
//
//    //Bean在初始化时会调用init方法
//    @Bean()
//    public ZookeeperRegistryCenter zookeeperRegistryCenter(@Value("${elaticjob.zookeeper.server-lists}") String serverList
//            , @Value("${elaticjob.zookeeper.namespace}") String namespace) {
//        ZookeeperRegistryCenter zookeeperRegistryCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(serverList, namespace));
//        zookeeperRegistryCenter.init();
//        return zookeeperRegistryCenter;
//    }
//
//}
//
