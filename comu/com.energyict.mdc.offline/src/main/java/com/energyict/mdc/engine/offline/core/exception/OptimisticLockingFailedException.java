package com.energyict.mdc.engine.offline.core.exception;

/**
 * Represent a concurrent modification exception
 */
public class OptimisticLockingFailedException extends BusinessException {


    /**
     * creates a new instance
     *
     * @param o Object that was the victim of the
     *          concurrent modification attempt
     */
    public OptimisticLockingFailedException(Object o) {
        super(
                "optimisticLockingFailed",
                "Object has been updated by an other user",
                o);
    }

    public Object getObject() {
        return this.arguments[0];
    }
}


