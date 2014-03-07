package com.elster.jupiter.issue.share.entity;

public class OperationResult<T, F> {
    private boolean fail;
    protected T successData;
    protected F failReason;

    public OperationResult() {
    }

    public static <T,F> OperationResult success(T data) {
        OperationResult<T,F> result = new OperationResult<T,F>();
        result.setSuccess(data);
        return result;
    }

    public static <T,F> OperationResult fail(F reason){
        OperationResult<T,F> result = new OperationResult<T,F>();
        result.setFail(reason);
        return result;
    }

    public OperationResult<T,F> setSuccess(T data) {
        this.successData = data;
        return this;
    }

    public OperationResult<T,F> setFail(F reason){
        this.fail = true;
        this.failReason = reason;
        return this;
    }

    public boolean isFailed() {
        return fail;
    }

    public T getData(){
        return this.successData;
    }

    public F getFailReason(){
        return this.failReason;
    }
}
