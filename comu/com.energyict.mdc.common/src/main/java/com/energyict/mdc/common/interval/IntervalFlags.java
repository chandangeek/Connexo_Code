package com.energyict.mdc.common.interval;

import java.util.ArrayList;
import java.util.List;

/**
 * IntervalFlags is a wrapper class for interval states
 */
public class IntervalFlags implements IntervalStateBits, Cloneable, Comparable {

    int flags;

    /**
     * Creates a new IntervalFlags object
     */
    public IntervalFlags() {
        this.flags = 0;
    }

    /**
     * Creates a new IntervalFlags object
     *
     * @param flags the wrapped flags value
     */
    public IntervalFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Returns the interval state
     *
     * @return the state
     */
    public int getValue() {
        return flags;
    }

    // make clone() public

    /**
     * Returns a clone.
     *
     * @return a clone
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Copies the receiver
     *
     * @return a copy
     */
    public IntervalFlags copy() {
        return (IntervalFlags) clone();
    }

    /**
     * Test if the indicated flag is set
     *
     * @param mask the zero based flag index
     * @return true if set , false otherwise
     */
    public boolean getFlag(int mask) {
        return (flags & mask) != 0;
    }

    /**
     * sets the indicated flag
     *
     * @param mask the zero based flag index
     */
    public void setFlag(int mask) {
        flags |= mask;
    }

    /**
     * Clears the indicated flag
     *
     * @param mask the zero base flag index
     */
    public void clearFlag(int mask) {
        flags &= ~mask;
    }

    /**
     * updates the indicated flag
     *
     * @param mask  the zero based flag index
     * @param value if true set the flag, else clear
     */
    public void setFlag(int mask, boolean value) {
        if (value) {
            setFlag(mask);
        } else {
            clearFlag(mask);
        }
    }

    /**
     * test if the power down flag is set
     *
     * @return true if the power down flag is set, false otherwise
     */
    public boolean getPowerDown() {
        return getFlag(POWERDOWN);
    }

    /**
     * reset/set the power down flag
     *
     * @param value true to set, false to reset
     */
    public void setPowerDown(boolean value) {
        setFlag(POWERDOWN, value);
    }

    /**
     * test if the power up flag is set
     *
     * @return true if the power up flag is set, false otherwise
     */
    public boolean getPowerUp() {
        return getFlag(POWERUP);
    }

    /**
     * reset/set the power up flag
     *
     * @param value true to set, false to reset
     */
    public void setPowerUp(boolean value) {
        setFlag(POWERUP, value);
    }

    /**
     * test if the 'short or long' flag is set
     *
     * @return true if the 'short or long' flag is set, false otherwise
     */
    public boolean getShortLong() {
        return getFlag(SHORTLONG);
    }

    /**
     * reset/set the 'short or long' flag
     *
     * @param value true to set, false to reset
     */
    public void setShortLong(boolean value) {
        setFlag(SHORTLONG, value);
    }

    /**
     * test if the watchdog reset flag is set
     *
     * @return true if the watchdog reset flag is set, false otherwise
     */
    public boolean getWatchDogReset() {
        return getFlag(WATCHDOGRESET);
    }

    /**
     * reset/set the watchdog reset flag
     *
     * @param value true to set, false to reset
     */
    public void setWatchDogReset(boolean value) {
        setFlag(WATCHDOGRESET, value);
    }

    /**
     * test if the configuration change flag is set
     *
     * @return true if the config change flag is set, false otherwise
     */
    public boolean getConfigurationChange() {
        return getFlag(CONFIGURATIONCHANGE);
    }

    /**
     * reset/set the configuration change flag
     *
     * @param value true to set, false to reset
     */
    public void setConfigurationChange(boolean value) {
        setFlag(CONFIGURATIONCHANGE, value);
    }

    /**
     * test if the corrupted flag is set
     *
     * @return true if the corrupted flag is set, false otherwise
     */
    public boolean getCorrupted() {
        return getFlag(CORRUPTED);
    }

    /**
     * reset/set the corrupted flag
     *
     * @param value true to set, false to reset
     */
    public void setCorrupted(boolean value) {
        setFlag(CORRUPTED, value);
    }

    /**
     * test if the overflow flag is set
     *
     * @return true if the overflow flag is set, false otherwise
     */
    public boolean getOverflow() {
        return getFlag(OVERFLOW);
    }

    /**
     * reset/set the overflow flag
     *
     * @param value true to set, false to reset
     */
    public void setOverflow(boolean value) {
        setFlag(OVERFLOW, value);
    }

    /**
     * test if the estimated flag is set
     *
     * @return true if the estimated flag is set, false otherwise
     */
    public boolean getEstimated() {
        return getFlag(ESTIMATED);
    }

    /**
     * reset/set the estimated flag
     *
     * @param value true to set, false to reset
     */
    public void setEstimated(boolean value) {
        setFlag(ESTIMATED, value);
    }

    /**
     * test if the missing flag is set
     *
     * @return true if the missing flag is set, false otherwise
     */
    public boolean getMissing() {
        return getFlag(MISSING);
    }

    /**
     * reset/set the missing flag
     *
     * @param value true to set, false to reset
     */
    public void setMissing(boolean value) {
        setFlag(MISSING, value);
    }

    /**
     * test if the modified flag is set
     *
     * @return true if the modified flag is set, false otherwise
     */
    public boolean getModified() {
        return getFlag(MODIFIED);
    }

    /**
     * reset/set the modified flag
     *
     * @param value true to set, false to reset
     */
    public void setModified(boolean value) {
        setFlag(MODIFIED, value);
    }

    /**
     * test if the revised flag is set
     *
     * @return true if the revised flag is set, false otherwise
     */
    public boolean getRevised() {
        return getFlag(REVISED);
    }

    /**
     * reset/set the revised flag
     *
     * @param value true to set, false to reset
     */
    public void setRevised(boolean value) {
        setFlag(REVISED, value);
    }

    /**
     * test if the other flag is set
     *
     * @return true if the other flag is set, false otherwise
     */
    public boolean getOther() {
        return getFlag(OTHER);
    }

    /**
     * reset/set the other flag
     *
     * @param value true to set, false to reset
     */
    public void setOther(boolean value) {
        setFlag(OTHER, value);
    }

    /**
     * returns all description IDs applying for this IntervalFlags
     *
     * @return all description IDs applying for this IntervalFlags
     */
    public List getDescriptions() {
        List<String> result = new ArrayList<>();
        if ((flags & IntervalStateBits.POWERDOWN) != 0) {
            result.add("POWERDOWN");
        }
        if ((flags & IntervalStateBits.POWERUP) != 0) {
            result.add("POWERUP");
        }
        if ((flags & IntervalStateBits.SHORTLONG) != 0) {
            result.add("SHORTLONG");
        }
        if ((flags & IntervalStateBits.WATCHDOGRESET) != 0) {
            result.add("WATCHDOGRESET");
        }
        if ((flags & IntervalStateBits.CONFIGURATIONCHANGE) != 0) {
            result.add("CONFIGURATIONCHANGE");
        }
        if ((flags & IntervalStateBits.CORRUPTED) != 0) {
            result.add("CORRUPTED");
        }
        if ((flags & IntervalStateBits.OVERFLOW) != 0) {
            result.add("OVERFLOW");
        }
        if ((flags & IntervalStateBits.ESTIMATED) != 0) {
            result.add("ESTIMATED");
        }
        if ((flags & IntervalStateBits.MISSING) != 0) {
            result.add("MISSING");
        }
        if ((flags & IntervalStateBits.MODIFIED) != 0) {
            result.add("MODIFIED");
        }
        if ((flags & IntervalStateBits.REVISED) != 0) {
            result.add("REVISED");
        }
        if ((flags & IntervalStateBits.OTHER) != 0) {
            result.add("OTHER");
        }
        if ((flags & IntervalStateBits.REVERSERUN) != 0) {
            result.add("REVERSERUN");
        }
        if ((flags & IntervalStateBits.PHASEFAILURE) != 0) {
            result.add("PHASEFAILURE");
        }
        if ((flags & IntervalStateBits.BADTIME) != 0) {
            result.add("BADTIME");
        }
        if ((flags & IntervalStateBits.INITIALFAILVALIDATION) != 0) {
            result.add("INITIALFAILVALIDATION");
        }
        if ((flags & IntervalStateBits.CURRENTFAILVALIDATION) != 0) {
            result.add("CURRENTFAILVALIDATION");
        }
        if ((flags & IntervalStateBits.DEVICE_ERROR) != 0) {
            result.add("DEVICE_ERROR");
        }
        if ((flags & IntervalStateBits.BATTERY_LOW) != 0) {
            result.add("BATTERY_LOW");
        }
        if ((flags & IntervalStateBits.TEST) != 0) {
            result.add("TEST");
        }
        return result;
    }

    /**
     * test if the bad time flag is set
     *
     * @return true if the bad time flag is set, false otherwise
     */
    public boolean getBadTime() {
        return getFlag(BADTIME);
    }

    /**
     * reset/set the bad time flag
     *
     * @param value true to set, false to reset
     */
    public void setBadTime(boolean value) {
        setFlag(BADTIME, value);
    }

    /**
     * test if the failed validation flag is set
     *
     * @return true if the failed validation flag is set, false otherwise
     */
    public boolean getIninitialFailValidation() {
        return getFlag(INITIALFAILVALIDATION);
    }

    /**
     * reset/set the failed validation flag
     *
     * @param value true to set, false to reset
     */
    public void setInitialFailValidation(boolean value) {
        setFlag(INITIALFAILVALIDATION, value);
    }

    /**
     * test if the suspect flag is set
     *
     * @return true if the suspect flag is set, false otherwise
     */
    public boolean getCurrentFailValidation() {
        return getFlag(CURRENTFAILVALIDATION);
    }

    /**
     * reset/set the suspect flag
     *
     * @param value true to set, false to reset
     */
    public void setCurrentFailValidation(boolean value) {
        setFlag(CURRENTFAILVALIDATION, value);
    }

    /**
     * test if the reverse run flag is set
     *
     * @return true if the reverse run flag is set, false otherwise
     */
    public boolean getReverseRun() {
        return getFlag(REVERSERUN);
    }

    /**
     * reset/set the reverse run flag
     *
     * @param value true to set, false to reset
     */
    public void setReverseRun(boolean value) {
        setFlag(REVERSERUN, value);
    }

    /**
     * test if the phase failure flag is set
     *
     * @return true if the phase failure flag is set, false otherwise
     */
    public boolean getPhaseFailure() {
        return getFlag(PHASEFAILURE);
    }

    /**
     * reset/set the phase failure flag
     *
     * @param value true to set, false to reset
     */
    public void setPhaseFailure(boolean value) {
        setFlag(PHASEFAILURE, value);
    }

    /**
     * Compares this IntervalFlags object with another
     * (implementation of the Comparable interface)
     *
     * @param obj the object to compare this IntervalFlags object with
     * @return the result of the comparison
     */
    public int compareTo(Object obj) {
        return getValue() - ((IntervalFlags) obj).getValue();
    }

}
