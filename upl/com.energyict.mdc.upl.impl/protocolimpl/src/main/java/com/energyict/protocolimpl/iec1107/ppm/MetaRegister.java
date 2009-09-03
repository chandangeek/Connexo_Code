package com.energyict.protocolimpl.iec1107.ppm;

import java.math.BigDecimal;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.ppm.register.ScalingFactor;

/**
 * This class represents a register with it's meta information.
 * 
 * @author fbo
 */

public class MetaRegister {

	private String name = null;
	private BaseUnit baseUnit = null;
	private Unit unit = null;
	private MetaRegister sourceRegister = null;
	private ScalingFactor scalingFactor = null;
	private String registerFactoryKey = null;

	public MetaRegister(String name, String registerFactoryKey) {
		this.name = name;
		this.registerFactoryKey = registerFactoryKey;
	}

	public MetaRegister(String name, BaseUnit baseUnit) {
		this.name = name;
		this.baseUnit = baseUnit;
	}

	public MetaRegister(String name, String registerFactoryKey, BaseUnit baseUnit) {
		this(name, registerFactoryKey);
		this.baseUnit = baseUnit;
	}

	public String getName() {
		if (this.sourceRegister != null) {
			return this.name + " " + this.sourceRegister.name;
		}
		return this.name;
	}

	public Unit getUnit() {
		if (this.sourceRegister != null) {
			return this.sourceRegister.unit;
		}
		return this.unit;
	}

	private BaseUnit getBaseUnit() {
		if (this.sourceRegister != null) {
			return this.sourceRegister.baseUnit;
		}
		return this.baseUnit;
	}

	public MetaRegister getSourceRegister() {
		return this.sourceRegister;
	}

	public void setSourceRegister(MetaRegister sourceRegister) {
		this.sourceRegister = sourceRegister;
	}

	private ScalingFactor getScalingFactor() {
		if (this.sourceRegister != null) {
			return this.sourceRegister.scalingFactor;
		}
		return this.scalingFactor;
	}

	public void setScalingFactor(ScalingFactor scalingFactor) {
		this.scalingFactor = scalingFactor;
		this.unit = Unit.get(this.baseUnit.getDlmsCode(), scalingFactor.getUnitScale());
	}

	public Quantity scaleToRegister(long l) {
		BigDecimal value = this.scalingFactor.getRegisterScaleFactor().multiply(BigDecimal.valueOf(l));
		return new Quantity(value, getUnit());
	}

	public BigDecimal getRegisterScaleFactor() {
		if (getScalingFactor() != null) {
			return getScalingFactor().getRegisterScaleFactor();
		}
		return null;
	}

	public String getRegisterFactoryKey() {
		return this.registerFactoryKey;
	}

	public String toString() {
		return getName() + " " + getBaseUnit();
	}

	public String toLongString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName());

		sb.append(" [baseUnit=" + getBaseUnit() + "]");
		sb.append("[unit= " + getUnit() + "]");

		if (this.sourceRegister != null) {
			sb.append("[sourceRegister= " + this.sourceRegister.toString() + "]");
		} else {
			sb.append("[sourceRegister=<null> ]");
		}

		sb.append("[scalingFactor= " + this.scalingFactor + "]");

		return sb.toString();
	}

	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}

		if (!(obj instanceof MetaRegister)) {
			return false;
		}

		MetaRegister m2 = (MetaRegister) obj;

		if (this.registerFactoryKey == null) {
			if (m2.registerFactoryKey != null) {
				return false;
			}
		} else {
			if (!this.registerFactoryKey.equals(m2.registerFactoryKey)) {
				return false;
			}
		}

		if (this.name == null) {
			if (m2.name != null) {
				return false;
			}
		} else {
			if (!this.name.equals(m2.name)) {
				return false;
			}
		}

		if (this.baseUnit == null) {
			if (m2.baseUnit != null) {
				return false;
			}
		} else {
			if (!this.baseUnit.equals(m2.baseUnit)) {
				return false;
			}
		}

		return true;

	}

	public int hashCode() {
		int result = 0xFFFF;
		result = (this.registerFactoryKey == null) ? result : result ^ this.registerFactoryKey.hashCode();
		result = (this.name == null) ? result : result ^ this.name.hashCode();
		result = (this.baseUnit == null) ? result : result ^ this.baseUnit.hashCode();
		return result;
	}

}