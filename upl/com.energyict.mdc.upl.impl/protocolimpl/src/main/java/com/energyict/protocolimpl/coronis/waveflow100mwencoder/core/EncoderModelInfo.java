package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;


public class EncoderModelInfo {

	enum EncoderModelType {
		ServenTrent(0x02,"Severn Trent water meter absolute encoder"),
		Unknown(0xFF,"No encoder connected");
		
		int id;
		String description;
		
		
		EncoderModelType(final int id, final String description) {
			this.id=id;
			this.description=description;
		}
		public String toString() {
			return "EncoderModelType: "+description+", id "+Utils.toHexString(id);
		}
		
		static EncoderModelType fromId(final int id) throws WaveFlow100mwEncoderException {
			for (EncoderModelType o : values()) {
				if (o.id == id) {
					return o;
				}
			}
			throw new WaveFlow100mwEncoderException("Unknown encoder model type ["+Utils.toHexString(id)+"]");
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
        strBuff.append("   manufacturerId="+Utils.toHexString(getManufacturerId())+"\n");
        return strBuff.toString();
    }	 
	 
	final EncoderModelType getEncoderModelType() {
		return encoderModelType;
	}

	final int getManufacturerId() {
		return manufacturerId;
	}
	
	
}
