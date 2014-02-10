package com.elster.jupiter.metering.readings;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

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
			bits |= flag.creationMask();
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

    public Set<Flag> getFlags() {
        Set<Flag> set = EnumSet.noneOf(Flag.class);
        for (Flag flag : Flag.values()) {
            if (get(flag)) {
                set.add(flag);
            }
        }
        return set;
    }

	@Override
	public boolean equals(Object other) {
        return other instanceof ProfileStatus && bits == ((ProfileStatus) other).bits;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(bits);
	}
	
	public enum Flag {
		POWERDOWN("1.2.32"),
		POWERUP(null),
		SHORTLONG(null),
		WATCHDOGRESET("1.1.4"),
		CONFIGURATIONCHANGE("1.4.6"),
		CORRUPTED("1.1.7"),
		OVERFLOW("1.4.1"),
		RESERVED1(null),
		MISSING("1.5.259"),
		SHORT("1.4.2") {
			long creationMask() {
				return mask() | SHORTLONG.mask();
			}
		},
		LONG("1.4.3") {
			long creationMask() {
				return mask() | SHORTLONG.mask();
			}
		},
		OTHER(null),
		REVERSERUN("1.3.4"),  // ?
		PHASEFAILURE(null),
		BADTIME("1.1.9"),
		RESERVED4(null),
		RESERVED5(null),
		DEVICE_ERROR("1.1.3"),
		BATTERY_LOW("1.1.1"),
		TEST("1.4.5");

        private final String cimCode;

        private Flag(String cimCode) {
            this.cimCode = cimCode;
        }

        long mask() {
			return 1L << ordinal();
		}

        long creationMask() {
            return mask();
        }
	}
}
