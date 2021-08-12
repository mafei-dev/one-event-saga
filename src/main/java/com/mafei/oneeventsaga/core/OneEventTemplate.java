package com.mafei.oneeventsaga.core;

public interface OneEventTemplate<Aggregate> {
    void process(Class<?> aClass, Aggregate aggregate, Object... args);
}
