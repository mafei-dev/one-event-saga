package com.mafei.oneeventsaga.flows;

public interface OneEventLifeCycle<Aggregate> {
    void onError(Aggregate aggregate, Object... data);

    void onComplete(Aggregate aggregate, Object... data);

    void onStart(Aggregate aggregate, Object... data);
}
