package com.elster.jupiter.transaction;

public abstract class VoidTransaction implements Transaction<Void> {

    @Override
    public Void perform() {
        doPerform();
        return null;
    }

    protected abstract void doPerform();
}
