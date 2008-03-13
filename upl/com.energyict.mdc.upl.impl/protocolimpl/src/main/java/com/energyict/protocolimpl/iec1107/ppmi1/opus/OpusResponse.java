
package com.energyict.protocolimpl.iec1107.ppmi1.opus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;

/**Data object for grouping a meter-response; this consists of 
 * a single definition message and 0 .. n dataMessages. 
 * 
 * @author fbo */

public class OpusResponse {
	
	TimeZone timeZone = null;
	private byte [] identificationMessage = null; // KV 22072005 changed with setter & getter! 
	byte [] definitionMessage = null;
	ArrayList dataMessages = new ArrayList();
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	boolean isProfileData = false;
	
	OpusResponse( TimeZone timeZone, boolean isProfileData ){
		this.timeZone = timeZone;
		this.isProfileData = isProfileData;
	}

	void addDataMessage( byte [] aDataMessage ) throws IOException{
		dataMessages.add( aDataMessage );
		baos.write( aDataMessage );
	}
	
	/** for non 550 registers only !
	 * @throws IOException*/
	public byte [] getDataMessageContent( ) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for( int i = 0; i < dataMessages.size(); i ++ ) {
			ArrayList list = PPMUtils.split((byte[])dataMessages.get(i), 1);
			bos.write( (byte[])list.get(1) );
		}
		return ProtocolUtils.convert2ascii(bos.toByteArray());
	}
	
	protected boolean isDefinitionMessageValid( ){
		if( isProfileData ) 
			return isProfileDefinitionMessageValid();
		return true;
	}
	
	private boolean isProfileDefinitionMessageValid( ) {
		//Calendar currentIntervalTime = ProtocolUtils.getCalendar( timeZone );// KV 22072005 unused code
		List list = PPMUtils.split(definitionMessage, 1);

		String dayString = PPMUtils.parseBCDString((byte[]) list.get(2));
		String monthString = PPMUtils.parseBCDString((byte[]) list.get(3));

		int day = Integer.parseInt(dayString);
		int month = Integer.parseInt(monthString);

		return (day != 0 || month != 0);
	}
	
    // KV 22072005 changed with setter & getter!     
    public byte[] getIdentificationMessage() {
        return identificationMessage;
    }

    // KV 22072005 changed with setter & getter! 
    public void setIdentificationMessage(byte[] identificationMessage) {
        this.identificationMessage = identificationMessage;
    }
	
	
	
	
	
}
