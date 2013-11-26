package com.energyict.mdc.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BusinessEventManager implements TransactionResource {

    private CompositeBusinessEventListener listener;
    private List<BusinessEventListenerActivator> activators;  // Holds onto all BusinessEventListenerActivator until first event is fired

    public BusinessEventManager (List<? extends BusinessEventListenerActivator> activators) {
        this.listener = new CompositeBusinessEventListener();
        this.activators = new ArrayList<>(activators);
    }

    public void signalEvent (BusinessEvent event) throws BusinessException, SQLException {
        this.doInitialization();
        this.listener.handleEvent(event);
    }

    public void addListener (final BusinessEventListener listener) {
        this.doInitialization();
        this.listener = new CompositeBusinessEventListener(this.listener, listener);
    }

    private void doInitialization () {
        if (this.needsInitialization()) {
            List<BusinessEventListenerActivator> activators1 = this.activators;
            this.activators = null;
            for (BusinessEventListenerActivator activator : activators1) {
                activator.activateAt(this);
            }
        }
    }

    private boolean needsInitialization () {
        return this.activators != null;
    }

    public void begin () throws SQLException {
        this.listener.begin();
    }

    public void commit () throws SQLException {
        this.listener.commit();
    }

    public void rollback () throws SQLException {
        this.listener.rollback();
    }

    public void prepare () throws SQLException {
        this.listener.prepare();
    }

}