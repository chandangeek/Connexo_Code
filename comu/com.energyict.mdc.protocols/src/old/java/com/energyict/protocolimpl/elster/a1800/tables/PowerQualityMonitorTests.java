package com.energyict.protocolimpl.elster.a1800.tables;

import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 18-jan-2011
 * Time: 17:05:58
 */
public class PowerQualityMonitorTests extends AbstractTable {

    private byte[] pqm_logEntries;
    private byte[] pqm_logControl;
    private byte[] pqm_statusControl;
    private byte[] pqm_masterControl;
    private final static int TEST_LENGTH = 17;
    private final static int NUMBER_OF_TESTS = 32;
    private byte[] pqm_version = new byte[2];
    private byte pqm_revision;
    private byte pqm_config;
    private byte[][] pqm_tests = new byte[32][17];
    private byte[] pqm_testString;
    private byte[] pqm_minimumTime;

    /**
     * Creates a new instance of AbstractTable
     */
    public PowerQualityMonitorTests(TableFactory tableFactory) {
        super(tableFactory, new TableIdentification(48, true));
    }


    public byte getPqm_config() {
        return pqm_config;
    }

    public void setPqm_config(byte pqm_config) {
        this.pqm_config = pqm_config;
    }

    public byte getPqm_revision() {
        return pqm_revision;
    }

    public void setPqm_revision(byte pqm_revision) {
        this.pqm_revision = pqm_revision;
    }

    public byte[] getPqm_version() {
        return pqm_version;
    }

    public void setPqm_version(byte[] pqm_version) {
        this.pqm_version = pqm_version;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        int offset = 0;

        pqm_version = ProtocolTools.getSubArray(data, offset, offset + 2);
        offset += 2;

        pqm_revision = data[offset++];
        pqm_config = data[offset++];

        pqm_masterControl = ProtocolTools.getSubArray(data, offset, offset + 4);
        offset += 4;

        pqm_statusControl = ProtocolTools.getSubArray(data, offset, offset + 4);
        offset += 4;

        pqm_logControl = ProtocolTools.getSubArray(data, offset, offset + 4);
        offset += 4;

        pqm_logEntries = ProtocolTools.getSubArray(data, offset, offset + 2);
        offset += 2;

        for (int i = 0; i < NUMBER_OF_TESTS; i++) {
            pqm_tests[i] = ProtocolTools.getSubArray(data, offset, offset + TEST_LENGTH);
            offset += TEST_LENGTH;
        }

        pqm_testString = ProtocolTools.getSubArray(data, offset, offset + 15);
        offset += 15;

        pqm_minimumTime = ProtocolTools.getSubArray(data, offset, offset + 2);
    }

    /**
     * Indicates the length of the pqm log entries in the MT49 table.
     * (11 bytes if they contain a log value, 7 bytes if not.)
     *
     * @return
     */
    public boolean hasLogValue() {
        return (pqm_config & 0x02) != 0;
    }

    /**
     * 32 flags, indicates which of the 32 tests are active.
     * Currently, there's 12 tests active.
     * @return
     */
    public byte[] getPqm_masterControl() {
        return pqm_masterControl;
    }

    public boolean hasPQMFunctionality() {
        return (pqm_config & 0x01) != 0;
    }
}
