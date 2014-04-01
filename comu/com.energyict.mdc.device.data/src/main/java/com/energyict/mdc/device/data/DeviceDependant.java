package com.energyict.mdc.device.data;

/**
 * Models the behavior of a component that is interested to receive
 * notification of events that apply to {@link com.energyict.mdc.protocol.api.device.BaseDevice}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-05-23 (17:29)
 */
public interface DeviceDependant {

    /**
     * Notifies this component that the specified
     * {@link com.energyict.mdc.protocol.api.device.BaseDevice} is about to be deleted.
     *
     * @param device The Device that is about to be deleted
     */
    public void notifyDeviceDelete (Device device);

    /**
     * Notifies this component that the topology of
     * the specified {@link com.energyict.mdc.protocol.api.device.BaseDevice} changed.
     *
     * @param device The Device whose topology changed
     */
    public void topologyChanged (Device device);

}