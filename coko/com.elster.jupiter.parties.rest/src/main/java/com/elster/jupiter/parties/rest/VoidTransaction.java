package com.elster.jupiter.parties.rest;

import com.elster.jupiter.transaction.Transaction;

public abstract class VoidTransaction implements Transaction<Void> {

    @Override
    public Void perform() {
        doPerform();
        return null;
    }

    protected abstract void doPerform();
}
