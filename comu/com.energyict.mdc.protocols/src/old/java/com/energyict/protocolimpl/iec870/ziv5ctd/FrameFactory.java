package com.energyict.protocolimpl.iec870.ziv5ctd;

/** */

public class FrameFactory {

    AsduFactory asduFactory;
    Address address;

    FrameFactory( Address address, AsduFactory asduFactory ){
        this.asduFactory = asduFactory;
        this.address = address;        
    }

    Frame createFixed( FunctionCode functionCode ){
        ControlField controlField = new ControlField(functionCode);
        controlField.setPrm(true);
        FixedFrame fixedFrame = new FixedFrame( address, controlField );
        return fixedFrame;
    }

    Frame createVariable( FunctionCode functionCode, TypeIdentification typeIdentification ){
        ControlField controlField = new ControlField( functionCode );
        controlField.setPrm(true);
        Asdu asdu =  asduFactory.get(typeIdentification);
        asdu.add( InformationObjectFactory.createProfile(null, null) );
        return new VariableFrame(address, controlField, asdu );
    }
    
    Frame createVariable( FunctionCode functionCode, Asdu asdu ){
        ControlField controlField = new ControlField( functionCode );
        controlField.setPrm(true);
        return new VariableFrame(address, controlField, asdu );
    }

    Frame parse( ByteArray ba ) throws ParseException {

        if( ba.get(0) == Frame.START_FIXED ) {
            Address address = Address.parse( ba.sub(2,2) );
            ControlField controlField = ControlField.parse(ba.get(0));
            FixedFrame ff = new FixedFrame( address, controlField, ba );
            return ff;
        }

        if( ba.get(0) == Frame.START_VARIABLE ) {
            
            if( ba.intValue(1) != ba.intValue(2) ) {
                String msg = "Variable Frame length fields are different"; 
                throw new ParseException( msg );
            }
            
            int length = ba.intValue(1);
            
            Address address = Address.parse( ba.sub(5, 6) );
            ControlField controlField = ControlField.parse(ba.get(4) );
            Asdu asdu = asduFactory.parse(ba.sub(7, length - 2 ) );
            VariableFrame vf = new VariableFrame(address, controlField, asdu, ba );
            return vf;
        }

        String msg = "FrameFactory.parse() unknown frame type " + ba.get(0);
        throw
            new ParseException( msg );

    }

}
