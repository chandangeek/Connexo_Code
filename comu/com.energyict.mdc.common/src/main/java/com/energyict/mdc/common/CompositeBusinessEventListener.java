package com.energyict.mdc.common;

import java.sql.SQLException;

public class CompositeBusinessEventListener implements BusinessEventListener {

    private BusinessEventListener[] listeners;

    CompositeBusinessEventListener () {
        this.listeners = new BusinessEventListener[0];
    }

    CompositeBusinessEventListener (CompositeBusinessEventListener base, BusinessEventListener next) {
        if (canAddNextEventListener(base, next)) {
            this.listeners = new BusinessEventListener[base.listeners.length + 1];
            for (int i = 0; i < base.listeners.length; i++) {
                listeners[i] = base.listeners[i];
            }
            listeners[base.listeners.length] = next;
        }
        else {
            listeners = base.listeners;
        }
    }

    /**
     * Method the check whether the given BusinessEventListener can be added to the base CompositeBusinessEventListener
     *
     * @param base the {@link CompositeBusinessEventListener} containing all previous registered {@link BusinessEventListener}s
     * @param next the new {@link BusinessEventListener} to investigate
     * @return true, if the given BusinessEventListener is not yet present in the base CompositeBusinessEventListener
     *         false, if there was already a listener of the same type present
     */
    private boolean canAddNextEventListener (CompositeBusinessEventListener base, BusinessEventListener next) {
        for (int i = 0; i < base.listeners.length; i++) {
            if (base.listeners[i].getClass().equals(next.getClass())) {
                return false;
            }
        }
        return true;
    }

    public void handleEvent (BusinessEvent event) throws SQLException, BusinessException {
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].handleEvent(event);
        }
    }

    private TransactionResource[] getResources () {
        return listeners;
    }

    /**
     * {@inheritDoc}
     */
    public void begin () throws SQLException {
        TransactionResource[] resources = getResources();
        for (int i = 0; i < resources.length; i++) {
            resources[i].begin();
        }

    }

    /**
     * {@inheritDoc}
     */
    public void commit () throws SQLException {
        Throwable failure = null;
        TransactionResource[] resources = getResources();
        for (int i = 0; i < resources.length; i++) {
            try {
                resources[i].commit();
            }
            catch (Throwable e) {
                if (failure != null) {
                    failure = e;
                }
            }
        }
        if (failure != null) {
            throw new ApplicationException(failure);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void prepare () throws SQLException {
        TransactionResource[] resources = getResources();
        for (int i = 0; i < resources.length; i++) {
            resources[i].prepare();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void rollback () throws SQLException {
        Throwable failure = null;
        TransactionResource[] resources = getResources();
        for (int i = 0; i < resources.length; i++) {
            try {
                resources[i].rollback();
            }
            catch (Throwable ex) {
                if (failure != null) {
                    failure = ex;
                }
            }
        }
        if (failure != null) {
            throw new ApplicationException(failure);
        }
    }

}