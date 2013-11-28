package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.callback.InstallService;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class InstallerListener implements TypeListener {
    private final OrmModule.TransactionInjection transactionInjection;

    public InstallerListener(OrmModule.TransactionInjection transactionInjection) {
        this.transactionInjection = transactionInjection;
    }

    @Override
    public <I> void hear(final TypeLiteral<I> type, TypeEncounter<I> encounter) {
        encounter.register(new InjectionListener<I>() {
            @Override
            public void afterInjection(final I injectee) {
                System.out.println("Installing : " + type.getRawType().getName());
                transactionInjection.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((InstallService) injectee).install();
                    }
                });
            }
        });
    }
}
