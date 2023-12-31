package com.energyict.protocolimpl.powermeasurement.ion;

import com.energyict.protocolimpl.utils.ProtocolTools;

public class Authentication {

    private ByteArray byteArray;
    
    Authentication( String password, String userId ) throws InvalidPasswordException {
            
        // if either pwd or userid is entered, authentication is used
        // empty strings are regarded the same as null
        if( password == null || "".equals( password ) || 
            userId == null || "".equals( userId ) ) {
            
            String msg = 
                "For authentication both userId and password are required.\n" +
                "If you do not want to authenticate leave both userId and " +
                "password empty.";
            throw new InvalidPasswordException( msg );
        }
       
        Integer uid = parse( userId );
        
        if( uid == null ) {           // theoretically up to 255
            String msg = "A user id must be an integer value. (range 1-255)";
            throw new InvalidPasswordException( msg );
        }
        
        if( uid.intValue() < 0 || uid.intValue() > 255 ) {
            String msg = "A user id must be an integer ranging from 1 to 255";
            throw new InvalidPasswordException( msg );
        }
        
        Integer pwd = parse( password );

        if (pwd == null) {
            boolean invalidPassword = false;
            if (password.toLowerCase().startsWith("hex") && (password.length() > 15)) {
                invalidPassword = true;
            } else if (!password.toLowerCase().startsWith("hex") && (password.length() > 6)) {
                invalidPassword = true;
            }

            if (invalidPassword) {
                String msg = "An alphanumeric password is max. 6 chars long.";
                throw new InvalidPasswordException(msg);
            }
        }

        byteArray = new ByteArray()
                        .add( pwd != null ? (byte)0x00 : (byte)0x80 ) 
                        .add( (byte) uid.intValue() );

        if( pwd != null ) {
            byteArray.add((byte) 0x0).add((byte) 0x0).addRawInt(pwd.intValue(), 4);
        } else {
            byte[] passwordBytes;
            if (password.toLowerCase().startsWith("hex")) {
                passwordBytes = ProtocolTools.getBytesFromHexString(password.substring(3), "");
            } else {
                passwordBytes = password.getBytes();
            }

            for (int i = 0; i < (6 - passwordBytes.length); i++) {
                byteArray.add((byte) 0x00);
            }
            byteArray.add(passwordBytes);
        }
    }

    private Integer parse( String s ) {
        Integer rslt = null;
        try {
            rslt = Integer.valueOf( s );    
        } catch( NumberFormatException nfex ) {
            // it is not an integer, so return null
        }
        return rslt;
    }
    
    ByteArray toByteArray(){
        return byteArray;
    }
    
}
