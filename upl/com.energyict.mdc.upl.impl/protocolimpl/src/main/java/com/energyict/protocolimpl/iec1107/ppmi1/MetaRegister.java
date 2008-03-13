package com.energyict.protocolimpl.iec1107.ppmi1;

import java.math.BigDecimal;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.ppmi1.register.ScalingFactor;

/**
 * This class represents a register with it's meta information.
 * 
 * @author fbo
 */

public class MetaRegister {

	String name = null;
	BaseUnit baseUnit = null;
	Unit unit = null;
	MetaRegister sourceRegister = null;
	ScalingFactor scalingFactor = null;
	String registerFactoryKey = null;

	public MetaRegister(String name, String registerFactoryKey) {
		this.name = name;
		this.registerFactoryKey = registerFactoryKey;
	}

	public MetaRegister(String name, BaseUnit baseUnit) {
		this.name = name;
		this.baseUnit = baseUnit;
	}

	public MetaRegister(String name, String registerFactoryKey,
			BaseUnit baseUnit) {
		this(name, registerFactoryKey);
		this.baseUnit = baseUnit;
	}

	public String getName() {
		if (this.sourceRegister != null)
			return name + " " + sourceRegister.name;
		return name;
	}

	public Unit getUnit() {
		if (sourceRegister != null)
			return sourceRegister.unit;
		return unit;
	}

	private BaseUnit getBaseUnit() {
		if (sourceRegister != null)
			return sourceRegister.baseUnit;
		return baseUnit;
	}

	public MetaRegister getSourceRegister() {
		return sourceRegister;
	}

	public void setSourceRegister(MetaRegister sourceRegister) {
		this.sourceRegister = sourceRegister;
	}

	private ScalingFactor getScalingFactor() {
		if (sourceRegister != null)
			return sourceRegister.scalingFactor;
		return scalingFactor;
	}

	public void setScalingFactor(ScalingFactor scalingFactor) {
		this.scalingFactor = scalingFactor;
		this.unit = Unit.get(this.baseUnit.getDlmsCode(), scalingFactor
				.getUnitScale());
	}

	public Quantity scaleToRegister(long l) {
		BigDecimal value = scalingFactor.getRegisterScaleFactor().multiply(
				BigDecimal.valueOf(l));
		return new Quantity(value, getUnit());
	}

	public BigDecimal getRegisterScaleFactor() {
		if (getScalingFactor() != null)
			return getScalingFactor().getRegisterScaleFactor();
		return null;
	}

	public String getRegisterFactoryKey() {
		return registerFactoryKey;
	}

	public String toString() {
		return getName() + " " + getBaseUnit();
	}

	public String toLongString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName());

		sb.append(" [baseUnit=" + getBaseUnit() + "]");
		sb.append("[unit= " + getUnit() + "]");

		if (sourceRegister != null)
			sb.append("[sourceRegister= " + sourceRegister.toString() + "]");
		else
			sb.append("[sourceRegister=<null> ]");

		sb.append("[scalingFactor= " + scalingFactor + "]");

		return sb.toString();
	}

	public boolean equals(Object obj) {

		if (obj == this)
			return true;

		if (!(obj instanceof MetaRegister))
			return false;

		MetaRegister m2 = (MetaRegister) obj;

		if (registerFactoryKey == null ) {
			if( m2.registerFactoryKey != null )return false;
		} else {
			if (!registerFactoryKey.equals(m2.registerFactoryKey))
				return false;
		}
		
                if( name == null ) {
                    if( m2.name != null ) return false;
                } else {
                    if (!name.equals(m2.name))
			return false;
                }
                
                if( baseUnit == null ) {
                    if( m2.baseUnit != null ) return false;
                } else {
                    if (!baseUnit.equals(m2.baseUnit))
			return false;    
                }

		return true;

	}
        
        public int hashCode(){
            int result = 0xFFFF;
            result = (registerFactoryKey == null ) ? result : result ^ registerFactoryKey.hashCode();
            result = (name == null) ? result : result ^ name.hashCode();
            result = (baseUnit == null) ? result : result ^ baseUnit.hashCode();
            return result;
        }

}