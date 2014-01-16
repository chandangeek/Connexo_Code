package com.elster.jupiter.metering.readings;

import java.util.Objects;

public final class ProfileStatus {
	private final long bits;
	
	public ProfileStatus(long bits) {
		this.bits = bits;
		if (!isValid()) {
			throw new IllegalArgumentException("" + bits);
		}
	}
	
	public static ProfileStatus of(Flag... flags) {
		long bits = 0;
		for (Flag flag : flags) {
			bits |= flag.mask();
		}
		return new ProfileStatus(bits);
	}
	
	private boolean isValid() {
		if (get(Flag.SHORT) && get(Flag.LONG)) {
			return false;
		}
		if (get(Flag.SHORT) || get(Flag.LONG)) {
			if (!get(Flag.SHORTLONG)) {
				return false;
			}
		} 
		return true;
	}
	
	public boolean get(Flag flag) {
		return (bits & flag.mask()) != 0;
	}
	
	public long getBits() {
		return bits;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ProfileStatus) {
			return bits == ((ProfileStatus) other).bits;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(bits);
	}
	
	public enum Flag {
		POWERDOWN,
		POWERUP,
		SHORTLONG,
		WATCHDOGRESET,
		CONFIGURATIONCHANGE,
		CORRUPTED,
		OVERFLOW,
		RESERVED1,
		MISSING,
		SHORT {
			long mask() {
				return super.mask() | SHORTLONG.mask();
			}
		},
		LONG {
			long mask() {
				return super.mask() | SHORTLONG.mask();
			}
		},
		OTHER,
		REVERSERUN,
		PHASEFAILURE,
		BADTIME,
		RESERVED4,
		RESERVED5,
		DEVICE_ERROR,
		BATTERY_LOW,
		TEST;		
		
		long mask() {
			return 1L << ordinal();
		}
	}
}
