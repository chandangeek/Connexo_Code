/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.util.Objects;

public final class ProcessStatus {
	
	private final long bits;
	
	public ProcessStatus(long bits) {
		this.bits = bits;
	}
	
	public static ProcessStatus of(Flag... flags) {
		long bits = 0;
		for (Flag flag : flags) {
			bits |= flag.mask();
		}
		return new ProcessStatus(bits);
	}
	
	public ProcessStatus with(Flag... flags) {
		long newBits = bits;
		for (Flag flag : flags) {
			newBits |= flag.mask();
		}
		return new ProcessStatus(newBits);
	}
	
	public ProcessStatus withOut(Flag... flags) {
		long newBits = bits;
		for (Flag flag : flags) {
			newBits &= ~flag.mask();
		}
		return new ProcessStatus(newBits);
	}
	
	public ProcessStatus or(ProcessStatus other) {
		return new ProcessStatus(bits | other.getBits());
	}
	
	public boolean get(Flag flag) {
		return (bits & flag.mask()) != 0;
	}
	
	public long getBits() {
		return bits;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ProcessStatus) {
			return bits == ((ProcessStatus) other).bits;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(bits);
	}
	
	public enum Flag {
		QUALITY,
		SUSPECT,
		WARNING,
		CONFIRMED,
		EDITED,
		ESTIMATED;
		
		long mask() {
			return 1L << ordinal();
		}
	}

}
