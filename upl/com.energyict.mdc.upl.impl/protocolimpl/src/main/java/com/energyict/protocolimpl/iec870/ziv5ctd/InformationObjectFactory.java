package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Date;

/** */

public class InformationObjectFactory {

    private static final InformationObjectFactory instance = new InformationObjectFactory();
    
    static InformationObject createProfile( Date from, Date to ) {
        return instance.new HardCodedInformationObject();
    }

    static InformationObject parseType0x8(ByteArray byteArray) {
        return new InformationObject(  );
    }

    class HardCodedInformationObject extends InformationObject {
         public ByteArray toByteArray() {
            return new ByteArray(
                new byte[]{
                    0x0b, 0x01, 0x08, 0x01, 0x00, 0x09, 0x03,
                    0x06, 0x00, 0x00, 0x0a, 0x03, 0x06});
        }
    }
    
    class InformationObject1 extends InformationObject {
        public ByteArray toByteArray() {
            return null;
        }
    }

    class InformationObject2 extends InformationObject {
        public ByteArray toByteArray() {
            return null;
        }
    }

    class InformationObject3 extends InformationObject {
        public ByteArray toByteArray() {
            return null;
        }
    }

    class InformationObject4 extends InformationObject {
        public ByteArray toByteArray() {
            return null;
        }
    }

}
