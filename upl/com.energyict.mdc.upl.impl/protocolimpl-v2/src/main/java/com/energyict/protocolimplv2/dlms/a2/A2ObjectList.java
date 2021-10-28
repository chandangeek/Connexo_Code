package com.energyict.protocolimplv2.dlms.a2;

import com.energyict.dlms.UniversalObject;
import com.energyict.obis.ObisCode;

public class A2ObjectList {
    private UniversalObject[] objectList = {
            new UniversalObject(ObisCode.fromString("0.0.1.0.0.255").getLN(),8,0),
            new UniversalObject(ObisCode.fromString("0.0.1.1.0.101").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.1.1.0.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.2.1.0.255").getLN(),29,0),
            new UniversalObject(ObisCode.fromString("0.0.10.0.0.255").getLN(),9,0),
            new UniversalObject(ObisCode.fromString("0.0.10.0.106.255").getLN(),9,0),
            new UniversalObject(ObisCode.fromString("0.0.15.0.1.255").getLN(),22,0),
            new UniversalObject(ObisCode.fromString("0.0.16.2.0.255").getLN(),65,0),
            new UniversalObject(ObisCode.fromString("0.0.20.0.0.255").getLN(),19,0),
            new UniversalObject(ObisCode.fromString("0.0.25.0.0.255").getLN(),41,0),
            new UniversalObject(ObisCode.fromString("0.0.25.1.0.255").getLN(),42,0),
            new UniversalObject(ObisCode.fromString("0.0.25.3.0.255").getLN(),44,0),
            new UniversalObject(ObisCode.fromString("0.0.25.4.0.255").getLN(),45,0),
            new UniversalObject(ObisCode.fromString("0.0.25.6.0.255").getLN(),47,0),
            new UniversalObject(ObisCode.fromString("0.0.40.0.0.255").getLN(),15,0),
            new UniversalObject(ObisCode.fromString("0.0.40.0.1.255").getLN(),15,0),
            new UniversalObject(ObisCode.fromString("0.0.40.0.3.255").getLN(),15,0),
            new UniversalObject(ObisCode.fromString("0.0.41.0.0.255").getLN(),17,0),
            new UniversalObject(ObisCode.fromString("0.0.42.0.0.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.43.0.0.255").getLN(),64,0),
            new UniversalObject(ObisCode.fromString("0.0.43.0.1.255").getLN(),64,0),
            new UniversalObject(ObisCode.fromString("0.0.43.0.3.255").getLN(),64,0),
            new UniversalObject(ObisCode.fromString("0.0.43.0.13.255").getLN(),64,0),
            new UniversalObject(ObisCode.fromString("0.0.43.1.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.44.0.0.255").getLN(),18,0),
            new UniversalObject(ObisCode.fromString("0.0.66.0.128.255").getLN(),62,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.2.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.3.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.4.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.5.255").getLN(),21,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.6.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.7.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.8.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.11.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.14.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.20.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.21.255").getLN(),8192,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.22.255").getLN(),8192,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.25.255").getLN(),21,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.26.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.30.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.31.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.33.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.41.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.42.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.44.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.94.39.46.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.1.0.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.1.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.1.3.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.1.4.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.1.5.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.1.10.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.3.10.255").getLN(),70,0),
            new UniversalObject(ObisCode.fromString("0.0.96.6.0.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.0.96.6.4.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.0.96.6.5.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.6.6.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.0.96.8.0.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.0.96.10.2.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.11.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.11.2.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.14.0.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.15.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.15.2.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.20.25.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.20.30.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.53.0.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.97.97.3.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.1.43.1.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.1.96.6.0.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.1.96.6.4.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.1.96.6.5.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.1.96.6.6.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.128.96.194.1.255").getLN(),9136,0),
            new UniversalObject(ObisCode.fromString("0.128.96.194.2.255").getLN(),9137,0),
            new UniversalObject(ObisCode.fromString("0.128.96.194.101.255").getLN(),9138,0),
            new UniversalObject(ObisCode.fromString("7.0.0.1.0.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.0.0.2.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.0.0.2.8.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.0.0.8.23.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.0.0.9.3.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.0.0.64.0.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.0.0.64.29.255").getLN(),9149,0),
            new UniversalObject(ObisCode.fromString("7.0.0.64.59.255").getLN(),9132,0),
            new UniversalObject(ObisCode.fromString("7.0.0.64.254.255").getLN(),9128,0),
            new UniversalObject(ObisCode.fromString("7.0.12.2.0.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.12.26.0.101").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.13.2.0.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.13.2.1.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.13.2.2.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.13.2.3.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.13.26.0.101").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.41.0.0.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.41.2.0.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.43.2.0.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.43.41.0.255").getLN(),4,0),
            new UniversalObject(ObisCode.fromString("7.0.43.45.0.255").getLN(),4,0),
            new UniversalObject(ObisCode.fromString("7.0.96.5.0.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.0.96.5.1.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("7.0.98.11.0.255").getLN(),7,0),
            new UniversalObject(ObisCode.fromString("7.0.99.16.0.255").getLN(),7,0),
            new UniversalObject(ObisCode.fromString("7.0.99.98.0.255").getLN(),7,0),
            new UniversalObject(ObisCode.fromString("7.0.99.98.1.255").getLN(),7,0),
            new UniversalObject(ObisCode.fromString("7.0.99.99.2.255").getLN(),7,0),
            new UniversalObject(ObisCode.fromString("7.0.99.99.3.255").getLN(),7,0),
            new UniversalObject(ObisCode.fromString("7.0.99.99.4.255").getLN(),7,0),
            new UniversalObject(ObisCode.fromString("7.1.0.2.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.1.0.2.8.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.1.96.5.1.101").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.1.96.5.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.2.96.5.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.128.0.9.23.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("0.0.96.6.3.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.0.96.6.4.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.1.96.6.3.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.1.96.6.4.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.128.96.194.1.255").getLN(),9136,0),
            new UniversalObject(ObisCode.fromString("0.129.96.6.0.255").getLN(),3,0),
            new UniversalObject(ObisCode.fromString("0.129.96.194.101.255").getLN(),9138,0),
            new UniversalObject(ObisCode.fromString("7.0.0.64.28.255").getLN(),9149,0),
            new UniversalObject(ObisCode.fromString("7.0.0.64.29.255").getLN(),9149,0),
            new UniversalObject(ObisCode.fromString("7.2.0.2.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.2.0.2.8.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.3.0.2.1.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.3.0.2.8.255").getLN(),1,0),
            new UniversalObject(ObisCode.fromString("7.128.0.64.29.255").getLN(),9132,0),
    };

    public UniversalObject[] getObjectList() {
        return objectList;
    }
}
