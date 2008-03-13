
package com.energyict.protocolimpl.iec1107.ppm.opus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppm.PPMUtils;

/**Data object for grouping a meter-response; this consists of 
 * a single definition message and 0 .. n dataMessages. 
 * 
 * @author fbo */

public class OpusResponse {
	
	TimeZone timeZone = null;
	byte [] identificationMessage = null;
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
		List list = PPMUtils.split(definitionMessage, 1);

		String dayString = PPMUtils.parseBCDString((byte[]) list.get(2));
		String monthString = PPMUtils.parseBCDString((byte[]) list.get(3));

		int day = Integer.parseInt(dayString);
		int month = Integer.parseInt(monthString);

		return (day != 0 || month != 0);
	}
	
}
