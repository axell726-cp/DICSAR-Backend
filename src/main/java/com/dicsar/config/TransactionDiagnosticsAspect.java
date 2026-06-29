package com.dicsar.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionDiagnosticsAspect {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDiagnosticsAspect.class);

    @Around("@annotation(org.springframework.transaction.annotation.Transactional) || @within(org.springframework.transaction.annotation.Transactional)")
    public Object traceTransactionalMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        try {
            Object result = joinPoint.proceed();
            logRollbackOnlyIfPresent(method);
            return result;
        } catch (Throwable ex) {
            logger.error("Excepcion original en metodo transaccional {}. txActive={}, txName={}",
                    method,
                    TransactionSynchronizationManager.isActualTransactionActive(),
                    TransactionSynchronizationManager.getCurrentTransactionName(),
                    ex);
            throw ex;
        }
    }

    private void logRollbackOnlyIfPresent(String method) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }
        try {
            if (TransactionAspectSupport.currentTransactionStatus().isRollbackOnly()) {
                logger.error("La transaccion quedo marcada como rollback-only al salir de {}", method);
            }
        } catch (NoTransactionException ignored) {
            // El advisor transaccional puede estar por fuera de este aspecto.
        }
    }
}
