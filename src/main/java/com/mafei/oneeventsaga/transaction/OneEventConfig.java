package com.mafei.oneeventsaga.transaction;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static com.mafei.oneeventsaga.transaction.OneEventTxManager.ONE_EVENT_TX_MANAGER;

@Configuration
@EnableTransactionManagement
@EnableAutoConfiguration
public class OneEventConfig {
    public OneEventConfig() {
    }

    @Bean(ONE_EVENT_TX_MANAGER)
//    @Scope
    public OneEventTxManager oneEventTxManager() {
        return new OneEventTxManager();
    }
}
