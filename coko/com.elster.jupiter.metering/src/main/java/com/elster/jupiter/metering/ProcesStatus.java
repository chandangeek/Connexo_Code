package com.elster.jupiter.metering;

import java.util.Objects;

public final class ProcesStatus {
	
	private final long bits;
	
	public ProcesStatus(long bits) {
		this.bits = bits;
	}
	
	public static ProcesStatus of(Flag... flags) {
		long bits = 0;
		for (Flag flag : flags) {
			bits |= flag.mask();
		}
		return new ProcesStatus(bits);
	}
	
	public ProcesStatus with(Flag... flags) {
		long newBits = bits;
		for (Flag flag : flags) {
			newBits |= flag.mask();
		}
		return new ProcesStatus(newBits);
	}
	
	public ProcesStatus withOut(Flag... flags) {
		long newBits = bits;
		for (Flag flag : flags) {
			newBits &= ~flag.mask();
		}
		return new ProcesStatus(newBits);
	}
	
	public boolean get(Flag flag) {
		return (bits & flag.mask()) != 0;
	}
	
	public long getBits() {
		return bits;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ProcesStatus) {
			return bits == ((ProcesStatus) other).bits;
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
