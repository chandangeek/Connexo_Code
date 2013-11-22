package com.energyict.mdc.common.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.Environment;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;

/**
 * Acts as an adapter between the OSGi environment and the {@link EnvironmentImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-05 (15:09)
 */
@Component(name="com.energyict.mdw.environment", service = EnvironmentAdapter.class)
public class EnvironmentAdapter {
    private volatile DataSource dataSource;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile BundleContext context;

    public DataSource getDataSource () {
        return dataSource;
    }

    @Reference
    public void setDataSource (DataSource dataSource) {
        System.out.println("DataSource is being injected into the MDC environment: " + dataSource);
        this.dataSource = dataSource;
    }

    public TransactionService getTransactionService () {
        return transactionService;
    }

    @Reference
    public void setTransactionService (TransactionService transactionService) {
        System.out.println("Transaction service is being injected into the MDC environment: " + transactionService);
        this.transactionService = transactionService;
    }

    public ThreadPrincipalService getThreadPrincipalService () {
        return threadPrincipalService;
    }

    @Reference
    public void setThreadPrincipalService (ThreadPrincipalService threadPrincipalService) {
        System.out.println("Thread principle service is being injected into the MDC environment: " + threadPrincipalService);
        this.threadPrincipalService = threadPrincipalService;
    }

    public String getProperty (String propertyName) {
        return this.context.getProperty(propertyName);
    }

    @Activate
    public void activate (BundleContext context) {
        try {
            this.context = context;
            Bus.setEnvironmentAdapter(this);
            Environment.DEFAULT.set(new EnvironmentImpl());
            System.out.println("MDC environment is actived");
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Deactivate
    public void deactivate () {
        Bus.clearEnvironmentAdapter(this);
        Environment.DEFAULT.set(null);
        this.context = null;
        System.out.println("MDC environment is deactived");
    }

}
