package com.example.app.databaseConfig.DynamicDataSourceSettings.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.app.databaseConfig.DynamicDataSourceSettings.Context.TenantContext;
import com.example.app.databaseConfig.DynamicDataSourceSettings.TargetDataSourceRepo.TargetDataSource;

@Aspect
@Component
@Order(0)
public class AspectDataSource {
    
    @Around("@annotation(TargetDatabase)")
    public Object proceed(ProceedingJoinPoint pjp, TargetDataSource database) throws Throwable {
        String db = TenantContext.getCurrentTenant();
        try {
            TenantContext.setCurrentTenant(database.value());
            return pjp.proceed();
        } finally {
            if (db != null) {
                TenantContext.setCurrentTenant(db);;
            } else {
                TenantContext.clear();
            }
        }
    }

}
