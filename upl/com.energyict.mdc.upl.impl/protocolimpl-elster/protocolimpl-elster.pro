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

-keepnames class com.energyict.protocolimplv2.messages.convertor.A1MessageConverter
-keepnames class com.energyict.protocolimplv2.messages.convertor.EK280MessageConverter