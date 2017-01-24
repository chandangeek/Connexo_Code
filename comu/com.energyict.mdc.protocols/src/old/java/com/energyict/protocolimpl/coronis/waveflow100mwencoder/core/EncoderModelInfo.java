package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;


public class EncoderModelInfo {

	enum EncoderModelType {
		ServenTrent(0x02,EncoderInternalData.ENCODER_INTERNAL_DATA_LENGTH,"Severn Trent water meter absolute encoder"),
		ActarisMBusWater(0x01,ActarisMBusInternalData.MBUS_INTERNAL_DATA_LENGTH,"Actaris Static utrasonic mbus water meter"),
		Unknown(0xFF,-1,"No encoder connected");
		
		int id;
		String description;
		int internalDataBlockSize;
		
		final int getInternalDataBlockSize() {
			return internalDataBlockSize;
		}
		
		EncoderModelType(final int id, final int internalDataBlockSize, final String description) {
			this.id=id;
			this.internalDataBlockSize=internalDataBlockSize;
			this.description=description;
		}
		public String toString() {
			return "EncoderModelType: "+description+", id "+WaveflowProtocolUtils.toHexString(id);
		}
		
		static EncoderModelType fromId(final int id) throws WaveFlow100mwEncoderException {
			for (EncoderModelType o : values()) {
				if (o.id == id) {
					return o;
				}
			}
			throw new WaveFlow100mwEncoderException("Unknown encoder model type ["+WaveflowProtocolUtils.toHexString(id)+"]");
		}
	}	
	
	/**
	 * The encoder type model
	 */
	EncoderModelType encoderModelType;

	/**
	 * 2 first numeric digits of the serial code of the encoder
	 */
	int manufacturerId=0xff;

    EncoderModelInfo(EncoderModelType encoderModelType, int manufacturerId) {
		this.encoderModelType = encoderModelType;
		this.manufacturerId = manufacturerId;
	}
	
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EncoderModelInfo:\n");
        strBuff.append("   encoderModelType="+getEncoderModelType()+"\n");
        strBuff.append("   manufacturerId="+WaveflowProtocolUtils.toHexString(getManufacturerId())+"\n");
        return strBuff.toString();
    }	 
	 
	final EncoderModelType getEncoderModelType() {
		return encoderModelType;
	}

	final int getManufacturerId() {
		return manufacturerId;
	}
	
	
}
