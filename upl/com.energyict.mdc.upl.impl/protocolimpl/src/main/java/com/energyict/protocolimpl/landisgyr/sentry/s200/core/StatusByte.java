/*
 * StatusByte.java
 *
 * Created on 26 juli 2006, 17:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

/**
 *
 * @author Koen
 */
public class StatusByte {
    
    
    
    private boolean error;
    private boolean passwordError;
    private boolean commandFormatError;
    private boolean crcError;
    private boolean romFailure;
    private boolean programMalfunction;
    private boolean overrunError;
    private boolean framingError;
    private boolean ramFailure;
    
    /** Creates a new instance of StatusByte */
    public StatusByte(int status) {
        setRamFailure(((status >> 0) & 0x01) == 0x01);
        setFramingError(((status >> 1) & 0x01) == 0x01);
        setOverrunError(((status >> 2) & 0x01) == 0x01);
        setProgramMalfunction(((status >> 3) & 0x01) == 0x01);
        setRomFailure(((status >> 4) & 0x01) == 0x01);
        setCrcError(((status >> 5) & 0x01) == 0x01);
        setCommandFormatError(((status >> 6) & 0x01) == 0x01);
        setPasswordError(((status >> 7) & 0x01) == 0x01);
        if (status != 0) setError(true);
        else setError(false);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("StatusByte:\n");
        strBuff.append("   commandFormatError="+isCommandFormatError()+"\n");
        strBuff.append("   crcError="+isCrcError()+"\n");
        strBuff.append("   error="+isError()+"\n");
        strBuff.append("   framingError="+isFramingError()+"\n");
        strBuff.append("   overrunError="+isOverrunError()+"\n");
        strBuff.append("   passwordError="+isPasswordError()+"\n");
        strBuff.append("   programMalfunction="+isProgramMalfunction()+"\n");
        strBuff.append("   ramFailure="+isRamFailure()+"\n");
        strBuff.append("   romFailure="+isRomFailure()+"\n");
        return strBuff.toString();
    }
    
    public boolean isPasswordError() {
        return passwordError;
    }

    private void setPasswordError(boolean passwordError) {
        this.passwordError = passwordError;
    }

    public boolean isCommandFormatError() {
        return commandFormatError;
    }

    private void setCommandFormatError(boolean commandFormatError) {
        this.commandFormatError = commandFormatError;
    }

    public boolean isCrcError() {
        return crcError;
    }

    private void setCrcError(boolean crcError) {
        this.crcError = crcError;
    }

    public boolean isRomFailure() {
        return romFailure;
    }

    private void setRomFailure(boolean romFailure) {
        this.romFailure = romFailure;
    }

    public boolean isProgramMalfunction() {
        return programMalfunction;
    }

    private void setProgramMalfunction(boolean programMalfunction) {
        this.programMalfunction = programMalfunction;
    }

    public boolean isOverrunError() {
        return overrunError;
    }

    private void setOverrunError(boolean overrunError) {
        this.overrunError = overrunError;
    }

    public boolean isFramingError() {
        return framingError;
    }

    private void setFramingError(boolean framingError) {
        this.framingError = framingError;
    }

    public boolean isRamFailure() {
        return ramFailure;
    }

    private void setRamFailure(boolean ramFailure) {
        this.ramFailure = ramFailure;
    }

    public boolean isError() {
        return error;
    }

    private void setError(boolean error) {
        this.error = error;
    }
    
}
