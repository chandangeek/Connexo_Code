package com.energyict.mdc.engine.impl.web.events.commands;

/**
 * Provides code reuse opportunities for components
 * that implement the {@link Request} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-09 (17:29)
 */
public abstract class RequestImpl implements Request {

    private boolean binary = false;

    @Override
    public void setBinaryEvents (boolean flag) {
        this.binary = flag;
    }

    @Override
    public boolean useBinaryEvents () {
        return this.binary;
    }

}