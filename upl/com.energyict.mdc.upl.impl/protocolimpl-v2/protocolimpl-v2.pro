-dontshrink
-dontoptimize
-verbose
-ignorewarnings

# Keep - Applications. Keep all application classes that have a main method.
-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

# Keep annotations,
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes Signature
-keepattributes InnerClasses

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Also keep - Enumerations. Keep a method that is required in enumeration
# classes.
-keepclassmembers class * extends java.lang.Enum {
    public **[] values();
}

-keep interface *
-keep class * implements com.energyict.protocol.SmartMeterProtocol
-keep class * implements com.energyict.protocol.MeterProtocol
-keep class * implements com.energyict.mdc.protocol.DeviceProtocol
-keep class * implements com.energyict.mdc.protocol.inbound.InboundDeviceProtocol
-keep class * implements com.energyict.mdc.protocol.inbound.BinaryInboundDeviceProtocol
-keep class * implements com.energyict.mdc.protocol.inbound.ServletBasedInboundDeviceProtocol

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class test.com.*{
 public *;
}
-keepnames class com.energyict.protocolimplv2.eict.rtuplusserver.eiwebplus.RtuServer
-keepnames class com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155
-keepnames class com.energyict.protocolimplv2.coronis.muc.WebRTUWavenisGateway
-keepnames class com.energyict.protocolimplv2.coronis.waveflow.waveflowV2.WaveFlowV2
-keepnames class com.energyict.protocolimplv2.nta.elster.AM100
-keepnames class com.energyict.protocolimplv2.nta.elster.MbusDevice
-keepnames class com.energyict.protocolimplv2.ace4000.ACE4000Inbound
-keepnames class com.energyict.protocolimplv2.ace4000.ACE4000Outbound
-keepnames class com.energyict.protocolimplv2.eict.gatewayz3.GateWayZ3
-keepnames class com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer
-keepnames class com.energyict.protocolimplv2.dlms.g3.AS330D
-keepnames class com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100
-keep class com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100$ClientConfiguration {
        *;
}
-keep public class com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties
-keep public class com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.dlms.idis.am130.properties.IDISSecurityProvider
-keep public class com.energyict.protocolimplv2.dlms.idis.am130.properties.IDISSecurityProvider {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging
-keep public class com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging
-keep public class com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message.T210DMessaging
-keep public class com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message.T210DMessaging {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor
-keepnames class com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540MessageExecutor
-keep public class com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540MessageExecutor {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message.T210DMessageExecutor
-keep public class com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message.T210DMessageExecutor {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties
-keep public class com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100SecurityProvider
-keep public class com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100SecurityProvider {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataSerializer
-keep public class com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataSerializer {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130MessageExecutor
-keep public class com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130MessageExecutor {
	public *;
	protected *;
}
-keepnames class com.energyict.mdc.protocol.inbound.g3.BeaconPSKProvider
-keepnames class com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport
-keep public class com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport {
	public *;
	protected *;
}
-keepnames class com.energyict.mdc.protocol.inbound.g3.DummyComChannel
-keepnames class com.energyict.mdc.protocol.inbound.g3.EventPushNotificationParser
-keep public class com.energyict.mdc.protocol.inbound.g3.EventPushNotificationParser {
	public *;
	protected *;
}
-keep public class com.energyict.mdc.protocol.inbound.g3.BeaconPSKProvider {
	public *;
	protected *;
}
-keep public class com.energyict.mdc.protocol.inbound.g3.Beacon3100PushEventNotification {
	public *;
	protected *;
}
-keepnames class com.energyict.mdc.protocol.inbound.idis.T210DPushEventNotification
-keep public class com.energyict.mdc.protocol.inbound.idis.T210DPushEventNotification {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.RtuPlusServerMessages {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayConfigurationSupport {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.* {
   public *;
   protected *;
   private *;
}
-keep public class com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects.* {
   public *;
   protected *;
   private *;
}
-keep public class com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.* {
   public *;
   protected *;
   private *;
}
-keepnames class com.energyict.protocolimplv2.eict.rtuplusserver.idis.RtuPlusServer
-keepnames class com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP
-keepnames class com.energyict.protocolimplv2.nta.dsmr50.elster.am540.AM540
-keepnames class com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.MBusDevice
-keepnames class com.energyict.protocolimplv2.edp.CX20009
-keepnames class com.energyict.protocolimplv2.edp.JanzB280
-keepnames class com.energyict.protocolimplv2.elster.garnet.GarnetConcentrator
-keepnames class com.energyict.protocolimplv2.elster.garnet.A100C
-keepnames class com.energyict.protocolimplv2.dlms.idis.am500.AM500
-keepnames class com.energyict.protocolimplv2.dlms.idis.am500.MBusDevice
-keepnames class com.energyict.protocolimplv2.dlms.idis.am130.AM130
-keepnames class com.energyict.protocolimplv2.dlms.idis.am130.MBusDevice
-keepnames class com.energyict.protocolimplv2.eict.webrtuz3.WebRTUZ3
-keepnames class com.energyict.protocolimplv2.eict.webrtuz3.MBusDevice
-keepnames class com.energyict.protocolimplv2.eict.webrtuz3.EMeter

-keepnames class com.energyict.mdc.protocol.inbound.DlmsSerialNumberDiscover
-keepnames class com.energyict.protocolimpl.edmi.mk10.MK10InboundDeviceProtocol
-keepnames class com.energyict.protocolimplv2.elster.ctr.MTU155.discover.CtrInboundDeviceProtocol

-keepnames class com.energyict.protocolimplv2.elster.ctr.MTU155.CTRDeviceProtocolCache

-keepnames class com.energyict.protocolimplv2.abnt.elster.A1055
-keepnames class com.energyict.protocolimplv2.abnt.common.structure.field.UnitField {
    public *;
}

-keepnames class com.energyict.protocolimplv2.eict.gateway.TransparentGateway

# Used for Crypto server project
-keepnames class com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging
-keep public class com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor {
   public *;
   protected *;
}
-keepnames class com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor
-keep public class com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider{
   public *;
   protected *;
}
-keepnames class com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties
-keepnames class com.energyict.protocolimplv2.security.SecurityPropertySpecName

-keepnames class com.energyict.protocolimplv2.edmi.mk10.MK10
-keepnames class com.energyict.protocolimplv2.edmi.MK6.MK6