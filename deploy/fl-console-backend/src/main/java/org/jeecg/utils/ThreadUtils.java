package org.jeecg.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

public class ThreadUtils {
    private static ExecutorService service;

    public static ExecutorService getInstance() {

        if (service == null) {

            ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("thread-pool-%d").build();

            service = new ThreadPoolExecutor(10, 100, 0L,
                    TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(1024), threadFactory);
        }

        return service;
    }

}
