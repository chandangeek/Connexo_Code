package com.energyict.mdc.protocol;

/**
 * This type can be used by the protocols to determine which connection layer should be used.
 * <p>
 *
 * Date: 7/11/13
 * Time: 9:27
 * Author: khe
 */
public enum ComChannelType {

    Invalid(-1),
    SerialComChannel(0),            //HDLC connection layer
    OpticalComChannel(1),           //HDLC connection layer
    SocketComChannel(2),            //TCP connection layer
    DatagramComChannel(3),          //UDP connection layer
    WavenisGatewayComChannel(4),
    WavenisSerialComChannel(5),
    ProximusSmsComChannel(6),
    WebServiceComChannel(7);

    public static final String TYPE = "ComChannelType";
    private Integer type;

    ComChannelType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public boolean is(ComChannel comChannel) {
        Integer type = comChannel.getProperties().<Integer>getTypedProperty(TYPE);
        if(type != null){
            return getType().equals(type);
        } else {
            // TODO workaround, this object should be moved to an API package so both protocols and ComServer can access it
            // See JIRA: https://jira.eict.vpdc/browse/EISERVERSG-4553
            return this.name().equals(comChannel.getClass().getSimpleName());
        }
    }

}
