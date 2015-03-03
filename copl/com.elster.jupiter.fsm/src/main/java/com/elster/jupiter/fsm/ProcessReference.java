package com.elster.jupiter.fsm;

/**
 * References an externally defined process.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (13:01)
 */
public interface ProcessReference {

    public String getDeploymentId();

    public String getProcessId();

}