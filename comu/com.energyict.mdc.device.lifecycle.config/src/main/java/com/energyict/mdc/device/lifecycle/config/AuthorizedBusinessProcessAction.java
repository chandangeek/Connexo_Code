package com.energyict.mdc.device.lifecycle.config;

/**
 * Models an action that can be authorized to initiate an externally defined
 * busines process as defined by the {@link com.elster.jupiter.bpm.BpmService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (14:38)
 */
public interface AuthorizedBusinessProcessAction extends AuthorizedAction {

    /**
     * Models the parameters that will be passed to
     * the custom business process.
     */
    public enum ProcessParameterKey {
        DEVICE("device");

        private String name;

        ProcessParameterKey(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }



    public String getDeploymentId();

    public String getProcessId();

}