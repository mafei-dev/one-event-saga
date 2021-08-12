package com.mafei.oneeventsaga.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafei.oneeventsaga.annotations.Secondary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static com.mafei.oneeventsaga.transaction.OneEventTxManager.ONE_EVENT_TX_MANAGER;


@Component(ONE_EVENT_TX_MANAGER)
public class OneEventTxManager implements PlatformTransactionManager {
    public static final String ONE_EVENT_TX_MANAGER = "ONE_EVENT_TX_MANAGER";

//    private final ApplicationEventPublisher applicationEventPublisher;

    Logger logger = LoggerFactory.getLogger(OneEventTxManager.class);

    public OneEventTxManager() {
        System.out.println("OneEventTxManager.OneEventTxManager");
    }
    //    public OneEventTxManager(ApplicationEventPublisher applicationEventPublisher) {
//        this.applicationEventPublisher = applicationEventPublisher;
//    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition transactionDefinition) throws TransactionException {
        try {
            System.out.println("OneEventTxManager.getTransaction " + new ObjectMapper().writeValueAsString(transactionDefinition));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new OneEventSimpleTransactionStatus(transactionDefinition.getName());
    }

    @Override
    public void commit(TransactionStatus transactionStatus) throws TransactionException {
        System.out.println("OneEventTxManager.commit");
    }

    @Override
    public void rollback(TransactionStatus transactionStatus) throws TransactionException {
        OneEventSimpleTransactionStatus _transactionStatus = (OneEventSimpleTransactionStatus) transactionStatus;
        try {
            Class<?> serviceClass = Class.forName(_transactionStatus.getClassName().replaceFirst("\\.process", ""));
            Secondary secondary = serviceClass.getDeclaredAnnotation(Secondary.class);
            Object[] contextObjectSaveData = (Object[]) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()).getAttribute(Double.toString(secondary.step()), RequestAttributes.SCOPE_REQUEST);
            try {
                Method[] declaredMethods = serviceClass.getDeclaredMethods();
                Optional<Method> revert = Arrays.stream(declaredMethods).filter(method -> method.getName().equals("revert")).findFirst();
                Constructor<?> ctor = serviceClass.getConstructor();
                Object object = ctor.newInstance();
                revert.get().invoke(object, contextObjectSaveData);
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class OneEventSimpleTransactionStatus extends SimpleTransactionStatus {
    private final String className;

    OneEventSimpleTransactionStatus(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}