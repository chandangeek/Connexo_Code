/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link Reading} for a set of flags.
 * Note that the interpretation, i.e. the semantics of the flags
 * is not the {@link Register}'s nor the {@link Device}'s responsibility.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (13:14)
 */
@ProviderType
public interface FlagsReading extends Reading {

    long getFlags();

    /**
     * Returns the value of the nth flag in this FlagsReading
     * where counting starts at the least significant bit.<br>
     * Example: when the flag reading is 0x0100100101
     * then getFlagValue returns the following values:
     * <table>
     *     <tr><th>flagIndex</th><th>result</th></tr>
     *     <tr><td>0</td><td>true</td></tr>
     *     <tr><td>1</td><td>false</td></tr>
     *     <tr><td>2</td><td>true</td></tr>
     *     <tr><td>3</td><td>false</td></tr>
     *     <tr><td>4</td><td>false</td></tr>
     *     <tr><td>5</td><td>true</td></tr>
     *     <tr><td>6</td><td>false</td></tr>
     *     <tr><td>7</td><td>false</td></tr>
     *     <tr><td>8</td><td>true</td></tr>
     *     <tr><td>9</td><td>false</td></tr>
     * </table>
     *
     * @param flagIndex The index of the flag for which the value will be returned
     * @return The flag value
     */
    boolean getFlagValue(int flagIndex);

}