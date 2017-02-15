/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

public class EncoderUnitInfo {

	public enum EncoderUnitType {

		CubicMeters(0x01,"Cubic meters"),
		Liters(0x02,"Liters"),
		CubicFeet(0x03,"cubic feet"),
		ImperialGallons(0x05,"Imperial gallons"),
		USGallons(0x06,"US Gallons"),
		Unknown(0xFF,"No encoder connected");

		private int id;

		private String description;

		EncoderUnitType(final int id, final String description) {
			this.id=id;
			this.description=description;
		}

		public String toString() {
			return "EncoderUnitType: "+description+", id "+WaveflowProtocolUtils.toHexString(id);
		}

		static EncoderUnitType fromId(final int id) throws WaveFlow100mwEncoderException {
			for (EncoderUnitType o : values()) {
				if (o.id == id) {
					return o;
				}
			}
			throw new WaveFlow100mwEncoderException("Unknown encoder unit type ["+WaveflowProtocolUtils.toHexString(id)+"]");
		}

		final int getId() {
			return id;
		}

		final public Unit toUnit() {

			switch(this) {
				case CubicMeters:
					return Unit.get(BaseUnit.CUBICMETER);
				case Liters:
					return Unit.get(BaseUnit.LITER);
				case CubicFeet:
					return Unit.get(BaseUnit.CUBICFEET);
				case ImperialGallons:
					return Unit.get(BaseUnit.GALLON);
				case USGallons:
					return Unit.get(BaseUnit.US_GALLON);

				default:
					return Unit.get("");
			}
/*					CubicMeters(0x01,"Cubic meters"),
					Liters(0x02,"Liters"),
					CubicFeet(0x03,"cubic feet"),
					ImperialGallons(0x05,"Imperial gallons"),
					USGallons(0x06,"US Gallons"),
					Unknown(0xFF,"No encoder connected");*/

		}

	}

	/**
	 * The encoder type model
	 */
	EncoderUnitType encoderUnitType;

	/**
	 * Nr of the digits before the digital point
	 */
	int nrOfDigitsBeforeDecimalPoint;

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EncoderUnitInfo:\n");
        strBuff.append("   encoderUnitType="+getEncoderUnitType()+"\n");
        strBuff.append("   nrOfDigitsBeforeDecimalPoint="+getNrOfDigitsBeforeDecimalPoint()+"\n");
        return strBuff.toString();
    }

	EncoderUnitInfo(EncoderUnitType encoderUnitType,int nrOfDigitsBeforeDecimalPoint) {
		this.encoderUnitType = encoderUnitType;
		this.nrOfDigitsBeforeDecimalPoint = nrOfDigitsBeforeDecimalPoint;
	}

	final public EncoderUnitType getEncoderUnitType() {
		return encoderUnitType;
	}

	final public int getNrOfDigitsBeforeDecimalPoint() {
		return nrOfDigitsBeforeDecimalPoint;
	}





}

