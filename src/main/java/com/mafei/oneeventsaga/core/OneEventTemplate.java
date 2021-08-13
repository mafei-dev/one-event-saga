package com.mafei.oneeventsaga.core;

public interface OneEventTemplate<Aggregate> {
    OneEventResponse process(Class<?> aClass, Aggregate aggregate, Object... args);
}
