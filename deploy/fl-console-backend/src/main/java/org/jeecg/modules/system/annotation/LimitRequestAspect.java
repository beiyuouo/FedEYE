package org.jeecg.modules.system.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class LimitRequestAspect {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 定义切点
    // 让所有有@LimitRequest注解的方法都执行切面方法
    @Pointcut("@annotation(limitRequest)")
    public void excudeService(LimitRequest limitRequest) {
    }

    @Around("excudeService(limitRequest)")
    public Object doAround(ProceedingJoinPoint pjp, LimitRequest limitRequest) throws Throwable {
        // 获得request对象
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();

        Integer uCount =
                redisTemplate.opsForValue().get(request.getRemoteAddr()) == null ? 0 : (Integer) redisTemplate.opsForValue().get(request.getRemoteAddr());
        if (uCount >= limitRequest.count()) { // 超过次数，不执行目标方法
            //可以直接抛出异常
            return Result.error("请求过快，请一分钟后再试");
        } else if (uCount == 0) { // 第一次请求时，设置开始有效时间
            redisTemplate.opsForValue().increment(request.getRemoteAddr(), 1);
            redisTemplate.expire(request.getRemoteAddr(), 1, TimeUnit.MINUTES);
        } else { // 未超过次数， 记录数据加一
            redisTemplate.opsForValue().increment(request.getRemoteAddr(), 1);
        }
        // result的值就是被拦截方法的返回值
        Object result = pjp.proceed();
        return result;
    }

}
