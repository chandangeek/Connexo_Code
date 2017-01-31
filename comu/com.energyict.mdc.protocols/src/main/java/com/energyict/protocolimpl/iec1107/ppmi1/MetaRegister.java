/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.iec1107.ppmi1.register.ScalingFactor;

import java.math.BigDecimal;

/**
 * This class represents a register with it's meta information.
 *
 * @author fbo
 */
public class MetaRegister {

	private String			name				= null;
	private BaseUnit		baseUnit			= null;
	private Unit			unit				= null;
	private MetaRegister	sourceRegister		= null;
	private ScalingFactor	scalingFactor		= null;
	private String			registerFactoryKey	= null;

	/**
	 * @param name
	 * @param registerFactoryKey
	 */
	public MetaRegister(String name, String registerFactoryKey) {
		this.name = name;
		this.registerFactoryKey = registerFactoryKey;
	}

	/**
	 * @param name
	 * @param baseUnit
	 */
	public MetaRegister(String name, BaseUnit baseUnit) {
		this.name = name;
		this.baseUnit = baseUnit;
	}

	/**
	 * @param name
	 * @param registerFactoryKey
	 * @param baseUnit
	 */
	public MetaRegister(String name, String registerFactoryKey, BaseUnit baseUnit) {
		this(name, registerFactoryKey);
		this.baseUnit = baseUnit;
	}

	/**
	 * @return
	 */
	public String getName() {
		if (this.sourceRegister != null) {
			return name + " " + sourceRegister.name;
		}
		return name;
	}

	/**
	 * @return
	 */
	public Unit getUnit() {
		if (sourceRegister != null) {
			return sourceRegister.unit;
		}
		return unit;
	}

	/**
	 * @return
	 */
	private BaseUnit getBaseUnit() {
		if (sourceRegister != null) {
			return sourceRegister.baseUnit;
		}
		return baseUnit;
	}

	/**
	 * @return
	 */
	public MetaRegister getSourceRegister() {
		return sourceRegister;
	}

	/**
	 * @param sourceRegister
	 */
	public void setSourceRegister(MetaRegister sourceRegister) {
		this.sourceRegister = sourceRegister;
	}

	/**
	 * @return
	 */
	private ScalingFactor getScalingFactor() {
		if (sourceRegister != null) {
			return sourceRegister.scalingFactor;
		}
		return scalingFactor;
	}

	/**
	 * @param scalingFactor
	 */
	public void setScalingFactor(ScalingFactor scalingFactor) {
		this.scalingFactor = scalingFactor;
		this.unit = Unit.get(this.baseUnit.getDlmsCode(), scalingFactor.getUnitScale());
	}

	/**
	 * @param l
	 * @return
	 */
	public Quantity scaleToRegister(long l) {
		BigDecimal value = scalingFactor.getRegisterScaleFactor().multiply(BigDecimal.valueOf(l));
		return new Quantity(value, getUnit());
	}

	/**
	 * @return
	 */
	public BigDecimal getRegisterScaleFactor() {
		if (getScalingFactor() != null) {
			return getScalingFactor().getRegisterScaleFactor();
		}
		return null;
	}

	/**
	 * @return
	 */
	public String getRegisterFactoryKey() {
		return registerFactoryKey;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName() + " " + getBaseUnit();
	}

	/**
	 * @return
	 */
	public String toLongString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName());

		sb.append(" [baseUnit=" + getBaseUnit() + "]");
		sb.append("[unit= " + getUnit() + "]");

		if (sourceRegister != null) {
			sb.append("[sourceRegister= " + sourceRegister.toString() + "]");
		} else {
			sb.append("[sourceRegister=<null> ]");
		}

		sb.append("[scalingFactor= " + scalingFactor + "]");

		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof MetaRegister)) {
			return false;
		}

		MetaRegister m2 = (MetaRegister) obj;

		if (registerFactoryKey == null) {
			if (m2.registerFactoryKey != null) {
				return false;
			}
		} else {
			if (!registerFactoryKey.equals(m2.registerFactoryKey)) {
				return false;
			}
		}

		if (name == null) {
			if (m2.name != null) {
				return false;
			}
		} else {
			if (!name.equals(m2.name)) {
				return false;
			}
		}

		if (baseUnit == null) {
			if (m2.baseUnit != null) {
				return false;
			}
		} else {
			if (!baseUnit.equals(m2.baseUnit)) {
				return false;
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int result = 0xFFFF;
		result = (registerFactoryKey == null) ? result : result ^ registerFactoryKey.hashCode();
		result = (name == null) ? result : result ^ name.hashCode();
		result = (baseUnit == null) ? result : result ^ baseUnit.hashCode();
		return result;
	}

}