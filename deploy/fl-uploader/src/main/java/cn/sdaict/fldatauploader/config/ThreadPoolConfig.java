package cn.sdaict.fldatauploader.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    // 获取服务器的cpu个数
//    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();// 获取cpu个数
    private static final int CPU_COUNT = 8;// k8s pod形式部署获取的是1
    private static final int MAX_COUR_SIZE = CPU_COUNT * 2;

    // 接下来配置一个bean，配置线程池。
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(CPU_COUNT);// 设置核心线程数
        threadPoolTaskExecutor.setMaxPoolSize(MAX_COUR_SIZE);// 配置最大线程数
        threadPoolTaskExecutor.setQueueCapacity(MAX_COUR_SIZE * 10);// 配置队列容量（这里设置成最大线程数的十倍）
        threadPoolTaskExecutor.setThreadNamePrefix("test-thread");// 给线程池设置名称
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());// 设置任务的拒绝策略
        return threadPoolTaskExecutor;
    }
}
