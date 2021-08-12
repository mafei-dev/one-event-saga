package com.mafei.oneeventsaga.flows;

public interface ExternalServiceCommandWorkflow<Aggregate> {
    Object process(Aggregate aggregate, Object... data) throws Exception;

    Object revert(Aggregate aggregate, Object... data) throws Exception;
}
