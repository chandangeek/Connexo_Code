package com.energyict.mdc.device.data;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.tasks.ComTask;
import java.sql.SQLException;

/**
 * TODO Moved the ComTaskEnablement to the Device.Data bundle. Needs to be moved to the DeviceConfig bundle, but didn't want to
 * TODO do that because otherwise we hade a cyclic dependency (JP-482???)
 *
 * Enables the execution of a ComTask against Devices
 * of a {@link com.energyict.mdc.device.config.DeviceConfiguration} and specifies the security
 * requirements for that execution.
 * In addition, specifies preferred scheduling (e.g. every day or every week)
 * and preferred {@link com.energyict.mdc.device.config.PartialConnectionTask} or if the execution of the ComTask
 * should use the default ConnectionTask.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-21 (11:30)
 */
public interface ComTaskEnablement extends IdBusinessObject {

    public DeviceCommunicationConfiguration getDeviceCommunicationConfiguration ();

    public ComTask getComTask ();

    public SecurityPropertySet getSecurityPropertySet ();

    /**
     * Gets the preferred specifications for the calculation of the next
     * execution timestamp of ComTaskExecution.
     *
     * @return The NextExecutionSpecs
     */
    public NextExecutionSpecs getNextExecutionSpecs ();

    /**
     * Gets the flag that indicates if the ComTaskExecution
     * should ignore the {@link NextExecutionSpecs} and therefore
     * always execute in an inbound context.
     *
     * @return The flag that indicates if the NextExecutionSpecs should be ignored in an inbound context
     */
    public boolean isIgnoreNextExecutionSpecsForInbound ();

    /**
     * Gets the preferred execution priority for the execution
     * of the related ComTask on a Device
     * of the related {@link com.energyict.mdc.device.config.DeviceConfiguration}.
     * Remember that this is a positive number
     * and smaller numbers indicate higher priority.
     * Zero is therefore the absolute highest priority.
     *
     * @return The preferred priority
     */
    public int getPriority ();

    /**
     * Tests if this ComTaskEnablement is suspended.
     * Suspending a ComTaskEnablement will set
     * all ComTaskExecution on hold.
     *
     * @return A flag that indicates if this ComTaskEnablement is suspended
     */
    public boolean isSuspended ();

    /**
     * Suspends this ComTaskEnablement , i.e. temporarily
     * disables the execution of the related ComTask
     * on all Devices of the
     * related {@link com.energyict.mdc.device.config.DeviceConfiguration}.
     */
    public void suspend () throws BusinessException, SQLException;

    /**
     * Resumes this ComTaskEnablement , i.e. reverts any previous suspension.
     */
    public void resume () throws BusinessException, SQLException;

    /**
     * Tests if the execution of the related ComTask
     * on a Device of the related
     * {@link com.energyict.mdc.device.config.DeviceConfiguration} should use the default
     * ConnectionTask configured on that Device.
     *
     * @return <code>true</code> if the related ComTask should use the default ConnectionTask
     *         when executed against a Device, <code>false</code> otherwise.
     */
    public boolean useDefaultConnectionTask ();

    /**
     * Gets the {@link com.energyict.mdc.device.config.PartialConnectionTask} that specifies
     * the preferred way to setup a connection for the execution
     * of the related ComTask for all Devices
     * of the related {@link com.energyict.mdc.device.config.DeviceConfiguration}.
     *
     * @return The PartialConnectionTask
     */
    public PartialConnectionTask getPartialConnectionTask();


    /**
     * Gets the referenced ProtocolDialectConfigurationProperties object or null if none is linked.
     *
     * @return the referenced ProtocolDialectConfigurationProperties object or null if none is linked.
     */
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();

}