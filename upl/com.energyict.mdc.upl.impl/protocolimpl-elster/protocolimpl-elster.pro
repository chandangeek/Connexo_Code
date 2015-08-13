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

-keep interface *
-keep class * implements com.energyict.protocol.SmartMeterProtocol
-keep class * implements com.energyict.protocol.MeterProtocol

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembernames class * {
    native <methods>;
}

# The Dsfg driver
-keepnames class com.elster.protocolimpl.dsfg.Dsfg

# A summation of the Lis200 drivers
-keepnames class com.elster.protocolimpl.lis200.DL210

-keepnames class com.elster.protocolimpl.lis200.DL220

-keepnames class com.elster.protocolimpl.lis200.DL230

-keepnames class com.elster.protocolimpl.lis200.DL240

-keepnames class com.elster.protocolimpl.lis200.EK220

-keepnames class com.elster.protocolimpl.lis200.EK230

-keepnames class com.elster.protocolimpl.lis200.EK260

# DLMS EK280
-keepnames class com.elster.genericprotocolimpl.dlms.ek280.EK280
-keepnames class com.elster.protocolimpl.dlms.EK280

-keepnames class com.elster.protocolimpl.dlms.Dlms

# DLMS A1
-keepnames class com.elster.genericprotocolimpl.dlms.a1.A1
-keepnamles class com.elster.protocolimpl.dlms.A1

# LIS100 drivers
-keepnames class com.elster.protocolimpl.lis100.EK88
