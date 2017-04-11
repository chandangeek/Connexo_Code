#####################################################################
# You don't need to specify each and every protocol.
# Only add items which don't match any of the defined interfaces!
#####################################################################

-dontshrink
-dontoptimize
-verbose

# Keep - Applications. Keep all application classes that have a main method.
-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

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

#Keeping every:
#- DeviceProtocol
#- SmartMeterProtocol
#- MeterProtocol
#- InboundProtocol
#- MessageProtocol
#- ConnectionType
#- DeviceProtocolDialect
#- Legacy Message/SecuritySet convertors
#- CustomProperty related (PersistentDomainExtension)
-keep interface *
-keepnames class * implements com.energyict.mdc.upl.SmartMeterProtocol
-keepnames class * implements com.energyict.mdc.upl.MeterProtocol
-keepnames class * implements com.energyict.mdc.upl.DeviceProtocol
-keepnames class * implements com.energyict.mdc.upl.InboundDeviceProtocol
-keepnames class * implements com.energyict.mdc.upl.BinaryInboundDeviceProtocol
-keepnames class * implements com.energyict.mdc.upl.ServletBasedInboundDeviceProtocol
-keepnames class * implements com.energyict.protocol.MessageProtocol
-keepnames class * implements com.energyict.mdc.upl.io.ConnectionType
-keepnames class * implements com.energyict.mdc.upl.DeviceProtocolDialect
-keepnames class * implements com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities
-keepnames class * implements com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter
-keepnames class * implements com.energyict.mdc.upl.security.LegacySecurityPropertyConverter
-keepnames class * implements com.elster.jupiter.cps.PersistenceSupport{*;}
-keepclassmembers class * implements com.elster.jupiter.cps.PersistenceSupport{*;}
-keepnames class * implements com.elster.jupiter.cps.PersistentDomainExtension{*;}
-keepclassmembers class * implements com.elster.jupiter.cps.PersistentDomainExtension{*;}
-keepnames class * implements com.elster.jupiter.cps.CustomPropertySet{*;}
-keepclassmembers class * implements com.elster.jupiter.cps.CustomPropertySet{*;}

# Keep annotations,
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes Signature
-keepattributes InnerClasses

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keeps all Components so the Reference binding properly works
-keepnames @org.osgi.service.component.annotations.Component public class *
-keepclassmembers class * {
    @org.osgi.service.component.annotations.Reference *;
}
-keepclassmembers class * {
    @org.osgi.service.component.annotations.Activate *;
}

# Keep all dlms stuff
-keep public class com.energyict.protocolimpl.dlms.** {
  public protected *;
}

# These are 'plain' copies from what was in EIServer 9.x
-keep public class com.energyict.protocolimpl.dlms.common.NTASecurityProvider { public *; protected *;}
-keep public class com.energyict.protocolimpl.dlms.g3.G3Properties { public *; protected *;}
-keep public class com.energyict.protocolimpl.dlms.g3.profile.G3Profile { public *; protected *;}
-keep public class com.energyict.protocolimpl.dlms.g3.G3DeviceInfo { public *; protected *;}
-keep public class com.energyict.protocolimpl.dlms.g3.profile.G3Profile { public *; protected *;}
-keep public class com.energyict.protocolimpl.dlms.g3.events.* { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.Dsmr50MessageExecutor { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages.SagemComDsmr50MessageExecutor { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology { public *;}
-keep public class com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology { public *;}
-keep public class com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.AM540Messaging { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages.SagemComMessaging { public *; protected *;}
-keep public class com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessageExecutor { public *; protected *;}
-keep public class com.energyict.protocolimpl.generic.messages.MessageHandler { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor { public *; protected *;}
-keep public class com.energyict.protocolimpl.dlms.idis.events.* { public *; protected *;}
-keep public class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.topology.DeviceMappingRange { public *;}
-keep public class com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedRegister { public *;}
-keep public class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.events.EventsLog { public *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.AbstractEvent { public *;}
-keep public class com.energyict.protocolimpl.mbus.core.* { public *;}
-keep public class com.energyict.protocolimpl.dlms.* { public *;}
-keep public class com.energyict.protocolimpl.base.* { *;}
-keep public class com.energyict.protocolimpl.dlms.as220.*{ *;}
-keep public class com.energyict.protocolimpl.dlms.as220.emeter.*{ *;}
-keep public class com.energyict.protocolimpl.dlms.as220.gmeter.*{ *;}
-keep public class com.energyict.protocolimpl.dlms.as220.plc.*{ *;}
-keep public class com.energyict.protocolimpl.dlms.as220.plc.statistics.*{ *;}
-keep public class com.energyict.smartmeterprotocolimpl.eict.ukhub.* { *;}
-keep public class com.energyict.protocolimpl.generic.messages.MessageHandler
-keep public class com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging.* { *;}
-keep public class com.energyict.protocolimpl.iec1107.instromet.dl220.*{ *;}
-keep public class com.energyict.protocolimpl.iec1107.instromet.dl220.commands.*{ *;}
-keep public class com.energyict.protocolimpl.iec1107.instromet.dl220.objects.*{ *;}
-keep public class com.energyict.protocolimpl.iec1107.instromet.dl220.profile.*{ *;}
-keep public class com.energyict.protocolimpl.iec1107.instromet.dl220.registers.*{ *;}
-keep public class com.energyict.protocolimpl.messages.codetableparsing.CodeTableXml{ public *;}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.MbusControlLog { public *; protected *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.DisconnectControlLog { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events.AM540FraudDetectionLog { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events.AM540StandardEventLog { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events.AM540MBusLog { public *; }
-keep public class com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper { public *; protected *;}
-keep public class com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.RtuPlusServerMessages { public *; protected *;}
-keep public class com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties { public *; protected *;}
-keep public class com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.* { public *; protected *; private *;}
-keep public class com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects.* { public *; protected *; private *;}
-keep public class com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.* { public *; protected *; private *;}

-keepnames class com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController { public *; protected *;}
-keepnames class com.energyict.protocolimpl.dlms.g3.registers.*  { public *;}
-keepnames class com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper { public *; protected *;}
-keepnames class com.energyict.protocolimpl.dlms.idis.AM540ObjectList { public *;}
-keepnames class com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping { public *; protected *;}
-keepnames class com.energyict.protocolimpl.dlms.idis.xml.XMLParser { public *;}
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties { public *;}
-keepnames class com.energyict.protocolimplv2.abnt.common.structure.field.UnitField { public *;}
-keepnames class com.energyict.protocolimpl.dlms.idis.registers.* { public *;}
-keepnames class com.energyict.protocolimpl.dlms.idis.IDISObjectList { public *;}

-keepnames class com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover
-keepnames class com.energyict.protocolimpl.base.*
-keepnames class com.energyict.protocolimpl.base.RtuDiscoveredEvent
-keepnames class com.energyict.protocolimpl.coronis.core.RegisterCache
-keepnames class com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties
-keepnames class com.energyict.protocolimpl.dlms.common.NTASecurityProvider
-keepnames class com.energyict.protocolimpl.dlms.g3.G3SecurityProvider
-keepnames class com.energyict.protocolimpl.dlms.g3.registers.G3Mapping
-keepnames class com.energyict.protocolimpl.dlms.g3.registers.mapping.*
-keepnames class com.energyict.protocolimpl.generic.*
-keepnames class com.energyict.protocolimpl.iec1107identification.*
-keepnames class com.energyict.protocolimpl.meteridentification.*
-keepnames class com.energyict.protocolimplv2.elster.ctr.MTU155.CTRDeviceProtocolCache
-keepnames class com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedRegister
-keepnames class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.events.EventsLog
-keepnames class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.topology.DeviceMappingRange
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.*
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessageExecutor
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RespondingFrameCounterHandler
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40SecurityProvider
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGDLMSProfileIntervals
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MbusMessageExecutor
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.REMIDatalogger
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.Dsmr50Properties
-keepnames class com.energyict.protocols.mdc.services.impl.ProtocolsModule

# usages in ProtocolImpl-v2
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMeterInfo { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMbusSerialNumber { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.CapturedRegisterObject { public *; }
-keepnames class com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping {	public *; protected *; }
-keepnames class com.energyict.protocolimpl.dlms.g3.registers.mapping.*
-keepnames class com.energyict.protocolimpl.dlms.idis.xml.XMLParser { public *; }
-keep public class com.energyict.genericprotocolimpl.webrtu.common.MbusProvider { public *;	protected *; }

-keepclasseswithmembers public class com.energyict.protocolimpl.coronis.core.RegisterCache { public void cacheRegisters(java.util.List);}


-keepnames class com.energyict.protocolimpl.edmi.common.**  { public *; protected *; }
-keep class com.energyict.protocolimpl.edmi.mk10.registermapping.MK10RegisterInformation { *;}
-keep class com.energyict.protocolimpl.edmi.mk10.registermapping.ObisCodeMapper { public *; }

-keep class com.energyict.protocolimpl.edmi.mk6.registermapping.MK6RegisterInformation { *; }
-keep class com.energyict.protocolimpl.edmi.mk6.registermapping.ObisCodeMapper { public *; }
