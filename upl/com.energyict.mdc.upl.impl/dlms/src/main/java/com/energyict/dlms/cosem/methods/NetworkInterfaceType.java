package com.energyict.dlms.cosem.methods;

public enum NetworkInterfaceType {

    ALL(0, "All_Interfaces"),
    ETHERNET_WAN(1, "Ethernet_WAN"),
    ETHERNET_LAN(2, "Ethernet_LAN"),
    WIRELESS_WAN(3, "Wireless_WAN"),
    IP6_TUNNEL(4, "IP6_Tunnel"),
    PLC_NETWORK(5, "PLC_Network"),
    UNKNOWN(6, "Unknown");

    private final int networkType;
    private final String networkTypeInfo;
    public static final int SIZE = NetworkInterfaceType.values().length;

    private NetworkInterfaceType(int networkType, String networkTypeInfo) {
        this.networkType = networkType;
        this.networkTypeInfo = networkTypeInfo;
    }

    public String getNetworkInterfaceType() {
        return networkTypeInfo;
    }

    public int getNetworkType() {
        return networkType;
    }

    public static NetworkInterfaceType fromNetworkType(int networkType) {
        for (NetworkInterfaceType type : NetworkInterfaceType.values()) {
            if (type.getNetworkType() == networkType) {
                return type;
            }
        }
        return NetworkInterfaceType.UNKNOWN;
    }

    public enum State{
        DISABLED(0),
        ENABLED(1);
        State(int i) { }
    }
}