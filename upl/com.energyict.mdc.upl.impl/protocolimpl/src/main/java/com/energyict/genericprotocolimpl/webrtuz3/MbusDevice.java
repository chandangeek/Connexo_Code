package com.energyict.genericprotocolimpl.webrtuz3;

/**
 * @author gna
 *         Changes:
 *         GNA |27012009| Instead of using the nodeAddress as channelnumber we search for the channelnumber by looking at the mbusSerialNumbers
 *         GNA |28012009| Added the connect/disconnect messages. There is an option to enter an activationDate but there is no Object description for the
 *         Mbus disconnect controller yet ...
 *         GNA |04022009| Mbus connect/disconnect can be applied with a scheduler. We use 0.x.24.6.0.255 as the ControlScheduler and 0.x.24.7.0.255 as ScriptTable
 *         GNA |19022009| Added a message to change to connectMode of the disconnectorObject;
 *         Changed all messageEntrys in date-form to a UnixTime entry;
 * @deprecated generic protocols are no longer supported, a proper DeviceProtocol has been created to replace this
 */
public class MbusDevice {

}
