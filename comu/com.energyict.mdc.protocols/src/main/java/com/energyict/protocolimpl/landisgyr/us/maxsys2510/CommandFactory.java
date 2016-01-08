package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;

class CommandFactory {

    /**
     * @param crn command record number
     * @param tbn table number
     * @return new Command
     */
    XCommand createX(int crn, int cnms, int cnls) {
        XCommand c = new XCommand();
        c.setCrn(crn);
        c.setCnms(cnms);
        c.setCnls(cnls);
        return c;
    }

    /**
     * @param crn command record number
     * @param tbn table number
     * @return new Command
     */
    StandardCommand createY(int crn, int tbn) {
        StandardCommand c = new StandardCommand();
        c.setY();
        c.setCrn(crn);
        c.setTbn(tbn);
        return c;
    }

    StandardCommand createY(int crn, TableAddress tableAddress){
        StandardCommand c = new StandardCommand();
        c.setY();
        c.setCrn(crn);
        c.setTbn(tableAddress.getTable());
        c.setDpms( tableAddress.getDisplacement() / 256 );
        c.setDpls( tableAddress.getDisplacement() % 256 );
        c.setNbls( 1 );
        c.setNbms( 0 );
        return c;
    }

    /**
     * @param crn command record number
     * @param tbn table number
     * @return new Command
     */
    StandardCommand createZ(int crn, int tbn) {
        StandardCommand c = new StandardCommand();
        c.setZ();
        c.setCrn(crn);
        c.setTbn(tbn);
        return c;
    }


    BlockAcknowledgmentCommand createAck( int dbn ){
        return new BlockAcknowledgmentCommand((byte)0x06, dbn);
    }

    BlockAcknowledgmentCommand createNack( int dbn ){
        return new BlockAcknowledgmentCommand((byte)0x15, dbn);
    }


    Command parse( ByteArray byteArray ) throws IOException {

        byte [] c = byteArray.getBytes();

        if( c[1] == 0x0C ) {
            BlockCommand bc = new BlockCommand( );
            bc.setDbn( byteArray.intValue(7) );
            bc.setData( byteArray.sub( 8, 256 ));
            bc.setEot( c[c.length-1]==0x4 );

            return bc;
        }
        else {
        	throw new IOException("Communication error: Unexpected answer from the meter");
        }
    }


}

/**
 * The Z command
 *
 * The Z command data field specifies all variables in binary format.  Refer to
 * the following figure.
 *
 * Byte 4 TBN
 * specifies the Table Number, 1 to 255 to be loaded.  Table 0 is a read-only
 * table.
 *
 * Byte 5 DPMS
 * Most significant byte of the displacement into the specified table to
 * commence loading.  Loading into a table can begin at byte 0 to byte 65535.
 *
 * Byte 6 DPLS
 * Least significant byte of the displacement into the specified table to
 * commence loading.  Loading into a table can begin at byte 0 to byte 65535.
 *
 * Byte 7 NBMS
 * Most significant byte of number of blocks to load.
 *
 * Byte 8 NBLS
 * Least significant byte of number of blocks to load.
 *
 * Bytes 7 and 8 specify the number of 256 byte blocks to load into the
 * specified table beginning at the specified displacement.  If bytes 7 and 8
 * are both zero then the SMD will accept the number of bytes required to fill
 * up the specified table starting from the specified displacement.
 *
 * Byte 9 BLB
 * Byte 9 specifies the number of bytes to be used out of the last 256 byte
 * block.  If this byte is set to zero, all 256 bytes in the last block are to
 * be used as valid data.  The entire block is transmitted.  Any unused bytes
 * are discarded.
 *
 * @author fbo
 */

class StandardCommand extends Command {

    char cmd;
    int crn;
    int tbn;
    int dpms;
    int dpls;
    int nbms;
    int nbls;
    int blb;

    byte[] password;

    /**
     * @return
     */
    StandardCommand setY() {
        cmd = 'Y';
        return this;
    }

    /**
     * @return
     */
    StandardCommand setZ() {
        cmd = 'Z';
        return this;
    }

    /**
     * @return
     */
    boolean isY() {
        return 'Y' == cmd;
    }

    /**
     * @return
     */
    boolean isZ() {
        return 'Z' == cmd;
    }

    /**
     * @return command record number
     */
    int getCrn() {
        return crn;
    }

    /**
     * @param command
     *            record number
     */
    StandardCommand setCrn(int crn) {
        this.crn = crn;
        return this;
    }

    /**
     * @return number of bytes to be used of last 256 block
     */
    int getBlb() {
        return blb;
    }

    /**
     * @param blb
     *            number of bytes to be used of last 256 block
     */
    StandardCommand setBlb(int blb) {
        this.blb = blb;
        return this;
    }

    /**
     * @return least significant byte of the displacement into the table
     */
    int getDpls() {
        return dpls;
    }

    /**
     * @param dpls
     *            Least significant byte of the displacement into the table
     */
    StandardCommand setDpls(int dpls) {
        this.dpls = dpls;
        return this;
    }

    /**
     * @return most significant byte of the displacement into the table.
     */
    int getDpms() {
        return dpms;
    }

    /**
     * @param dpms
     *            most significant byte of the displacement into the table.
     */
    StandardCommand setDpms(int dpms) {
        this.dpms = dpms;
        return this;
    }

    /**
     * @return number of blocks to load least significant byte
     */
    int getNbls() {
        return nbls;
    }

    /**
     * @param nbls
     *            number of blocks to load least significant byte
     */
    StandardCommand setNbls(int nbls) {
        this.nbls = nbls;
        return this;
    }

    /**
     * @return number of blocks to load most significant byte
     */
    int getNbms() {
        return nbms;
    }

    /**
     * @param nbms
     *            number of blocks to load most significant byte
     */
    StandardCommand setNbms(int nbms) {
        this.nbms = nbms;
        return this;
    }

    /**
     * @return table number
     */
    int getTbn() {
        return tbn;
    }

    /**
     * @param tbn
     *            table number
     */
    StandardCommand setTbn(int tbn) {
        this.tbn = tbn;
        return this;
    }

    StandardCommand setPassword(byte[] pwd) {
        if (pwd.length != 4)
            throw new RuntimeException("Password must be 4 byte");
        this.password = pwd;
        return this;
    }

    byte[] getBytes() {
        byte[] r = new byte[15];
        r[0] = 0x02;
        r[1] = (byte) cmd;
        r[2] = (byte) crn;
        r[3] = (byte) tbn;
        r[4] = (byte) dpms;
        r[5] = (byte) dpls;
        r[6] = (byte) nbms;
        r[7] = (byte) nbls;
        r[8] = (byte) blb;
        r[9] = password[0];
        r[10] = password[1];
        r[11] = password[2];
        r[12] = password[3];

        int crc = 0x0000;
        for (int ri = 1; ri < 13; ri++) {
            byte c = r[ri];
            for (int i = 0; i < 8; i++) {
                boolean c15 = ((crc >> 15 & 1) == 1);
                boolean bit = ((c >> (7 - i) & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= 0x8005;
            }
        }

        r[13] = (byte)((crc>>8)&0xFF);
        r[14] = (byte)(crc&0x00FF);

        return r;
    }

    public ByteArray toByteArray() {
        return new ByteArray(getBytes());
    }

    boolean isStandardCommand( ){
        return true;
    }

    public String toString() {
        return "StandardCommand [ " + toByteArray().toHexaString(true) + " ]";
    }

}

class XCommand extends Command {

    int crn;
    int cnms;
    int cnls;
    byte[] argumnt = new byte [] { 0, 0, 0, 0 };
    byte[] password;

    /**
     * @return command record number
     */
    int getCrn() {
        return crn;
    }

    /**
     * @param command
     *            record number
     */
    XCommand setCrn(int crn) {
        this.crn = crn;
        return this;
    }

    /**
     * @param cnms
     */
    void setCnms(int cnms) {
        this.cnms = cnms;
    }

    /**
     * @param cnls
     */
    void setCnls(int cnls) {
        this.cnls = cnls;
    }

    /**
     * @param argumnt Command dependent.  If unused will be zero.
     */
    void setArgumnt(byte[] argumnt) {
        this.argumnt = argumnt;
    }

    XCommand setPassword(byte[] pwd) {
        if (pwd.length != 4)
            throw new RuntimeException("Password must be 4 byte");
        this.password = pwd;
        return this;
    }

    byte[] getBytes() {
        byte[] r = new byte[15];
        r[0] = 0x02;
        r[1] = (byte) 'X';
        r[2] = (byte) crn;
        r[3] = (byte) cnms;
        r[4] = (byte) cnls;
        r[5] = (byte) argumnt[0];
        r[6] = (byte) argumnt[1];
        r[7] = (byte) argumnt[2];
        r[8] = (byte) argumnt[3];
        r[9] = password[0];
        r[10] = password[1];
        r[11] = password[2];
        r[12] = password[3];

        int crc = 0x0000;
        for (int ri = 1; ri < 13; ri++) {
            byte c = r[ri];
            for (int i = 0; i < 8; i++) {
                boolean c15 = ((crc >> 15 & 1) == 1);
                boolean bit = ((c >> (7 - i) & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= 0x8005;
            }
        }

        r[13] = (byte)((crc>>8)&0xFF);
        r[14] = (byte)(crc&0x00FF);

        return r;
    }

    public ByteArray toByteArray() {
        return new ByteArray(getBytes());
    }

    public String toString() {
        return "XCommand [ " + toByteArray().toHexaString(true) + " ]";
    }

}

class BlockCommand extends Command {

    /* Data block number */
    int dbn;
    ByteArray data;
    /* End of transmission */
    boolean eot;

    int getDbn() {
        return dbn;
    }

    void setDbn(int dbn) {
        this.dbn = dbn;
    }

    boolean isEot() {
        return eot;
    }

    void setEot(boolean eot) {
        this.eot = eot;
    }

    ByteArray getData() {
        return data;
    }

    void setData(ByteArray data) {
        this.data = data;
    }

    public ByteArray toByteArray() {
        return null;
    }

    public boolean isBlockCommand( ){
        return true;
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();

        rslt.append( data.toHexaString(true));

        return rslt.toString();
    }

}

class BlockAcknowledgmentCommand extends Command {

    byte b2;
    byte dbn;

    BlockAcknowledgmentCommand( byte b2, int dbn ) {
        this.b2 = b2;
        this.dbn = (byte)dbn;
    }

    byte [] getBytes( ){
        byte[] r = new byte[5];
        r[0] = 0x02;
        r[1] = b2;
        r[2] = (byte) dbn;

        int crc = 0x0000;
        for (int ri = 1; ri < 3; ri++) {
            byte c = r[ri];
            for (int i = 0; i < 8; i++) {
                boolean c15 = ((crc >> 15 & 1) == 1);
                boolean bit = ((c >> (7 - i) & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= 0x8005;
            }
        }

        r[3] = (byte)((crc>>8)&0xFF);
        r[4] = (byte)(crc&0x00FF);

        return r;

    }

    public ByteArray toByteArray() {
        return new ByteArray(getBytes());
    }

    boolean isBlockAcknowledgmentCommand(){
        return true;
    }

    public String toString() {
        return "BlockAcknowledgmentCommand [ " + toByteArray().toHexaString(true) + " ]";
    }

}



