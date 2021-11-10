package com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums.G3NodeNodeModulationScheme;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums.G3NodePhaseInfo;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums.G3NodeTxModulation;

/**
 * Decodes a mac_neighbour_table from G3-PLC MAC setup IC
 *
 *
 * PIB attribute 0x010A: See [26] for CENELEC and FCC bands. The neighbour table contains
 * information about all the devices within the POS of the device. One element of the table
 * represents one PLC direct neighbour of the device. Contains array of neighbour_table.
 *
 * neighbour_table ::= structure
 * {
 *  short_address: long-unsigned,
 *  payload_modulation_scheme: boolean,
 *  tone_map: bit-string,
 *  modulation: enum:
 *          (0) Robust Mode,
 *          (1) DBPSK,
 *          (2) DQPSK,
 *          (3) D8PSK,
 *          (4) 16-QAM
 *  tx_gain: integer,
 *  tx_res: enum,
 *  tx_coeff: bit-string,
 *  lqi: unsigned,
 *  phase_differential: integer,
 *  TMR_valid_time: unsigned,
 *  neighbour_valid_time: unsigned
 * }
 *
 * Where:
 *
 * @short_address The MAC Short Address of the node which this entry refers to.
 * payload_modulation_scheme Payload Modulation scheme to be used when transmitting to
 * this neighbour. FALSE: Differential, TRUE: Coherent
 *
 * @tone_map The Tone Map parameter defines which frequency sub-band can be used for communication
 * with the device. A bit set to 1 means that the frequency sub-band can be
 * used, and a bit set to 0 means that frequency sub-band shall not be used.
 * modulation The modulation type to use for communicating with the device.
 *
 * @tx_gain Defines the Tx Gain to use to transmit frames to that device.
 *
 * @tx_res Defines the Tx Gain resolution corresponding to one gain step.
 * • 0 : 6 dB,
 * • 1 : 3 dB
 *
 * @tx_coeff A parameter that specifies transmitter gain for each group of tones represented by
 * one valid bit of the tone map. The receiver measures the frequency-dependent attenuation
 * of the channel and may request the transmitter to compensate for this attenuation
 * by increasing the transmit power on sections of the spectrum that are experiencing attenuation
 * in order to equalize the received signal. Each group of tones is mapped to a
 * 4-bit value for CENELEC-A or a 2-bit value for FCC where a ”0” in the most significant
 * bit indicates a positive gain value, hence an increase in the transmitter gain scaled by
 * TXRES is requested for that section and a ”1” indicates a negative gain value, hence
 * a decrease in the transmitter gain scaled by TXRES is requested for that section. Implementing
 * this feature is optional and it is intended for frequency selective channels. If
 * this feature is not implemented, the value zero shall be used.
 *
 * @lqi Link Quality Indicator of the link to the neighbour (reverse LQI)
 *
 * @phase_differential Phase difference in multiples of 60 degrees between the mains phase
 * of the local node and the neighbour node. PhaseDifferential can assume six integer
 * values between 0 and 5.
 *
 * @TMR_valid_time Remaining time in minutes until which the tone map response parameters
 * in the neighbour table are considered valid.
 * • When the entry is created, this value shall be set to the default value 0.
 * • When it reaches 0, a tone map request may be issued if data is sent to this device.
 * Upon successful reception of a tone map response, this value is set to mac_TMR_
 * TTL.
 *
 * @neighbour_valid_time Remaining time in minutes until which this entry in the neighbour
 * table is considered valid. Every time an entry is created or a frame (data or ACK) is
 * received from this neighbour, it is set to mac_neighbour_table_entry_TTL. When it
 * reaches zero, this entry is no longer valid in the table and may be removed.
 *
 *
 *
 */
public class G3Neighbor {

    private final long shortAddress;
    private final G3NodeNodeModulationScheme modulationSchema;
    private final long toneMap;
    private final G3NodeTxModulation modulation;
    private final int txGain;
    private final int txRes;
    private final int txCoeff;
    private final int lqi;
    private final G3NodePhaseInfo phaseDifferential;
    private final int tmrValidTime;
    private final int neighbourValidTime;


    public G3Neighbor(long shortAddress,
                      G3NodeNodeModulationScheme modulationSchema,
                      long toneMap,
                      G3NodeTxModulation modulation,
                      int txGain,
                      int txRes,
                      int txCoeff,
                      int lqi,
                      G3NodePhaseInfo phaseDifferential,
                      int tmrValidTime,
                      int neighbourValidTime) {
        this.shortAddress = shortAddress;
        this.modulationSchema = modulationSchema;
        this.toneMap = toneMap;
        this.modulation = modulation;
        this.txGain = txGain;
        this.txRes = txRes;
        this.txCoeff = txCoeff;
        this.lqi = lqi;
        this.phaseDifferential = phaseDifferential;
        this.tmrValidTime = tmrValidTime;
        this.neighbourValidTime = neighbourValidTime;
    }


    public static G3Neighbor fromStructure(final Structure neighbourStruct)  {
        final long shortAddress = neighbourStruct.getDataType(0).getUnsigned16().longValue();

        final boolean modulationSchemaAttr = neighbourStruct.getDataType(1).getBooleanObject().getState();
        final G3NodeNodeModulationScheme modulationSchema = G3NodeNodeModulationScheme.fromValue(modulationSchemaAttr);

        final long toneMap = neighbourStruct.getDataType(2).getBitString().longValue();
        
        int modulationAttr = neighbourStruct.getDataType(3).getTypeEnum().getValue();
        final G3NodeTxModulation modulation = G3NodeTxModulation.fromValue(modulationAttr);

        final int txGain = neighbourStruct.getDataType(4).getInteger8().getValue();
        final int txRes = neighbourStruct.getDataType(5).getTypeEnum().getValue();
        final int txCoeff = neighbourStruct.getDataType(6).getBitString().intValue();
        final int lqi = neighbourStruct.getDataType(7).getUnsigned8().intValue();

        int phaseDifferentialAttr = neighbourStruct.getDataType(8).getInteger8().intValue();
        final G3NodePhaseInfo phaseDifferential = G3NodePhaseInfo.fromValue(phaseDifferentialAttr);

        final int tmrValidTime = neighbourStruct.getDataType(9).getUnsigned8().intValue();
        final int neighbourValidTime = neighbourStruct.getDataType(10).getUnsigned8().intValue();

        return new G3Neighbor(shortAddress, modulationSchema,toneMap, modulation, txGain, txRes, txCoeff, lqi, phaseDifferential, tmrValidTime, neighbourValidTime);
    }

    public long getShortAddress() {
        return shortAddress;
    }

    public G3NodeNodeModulationScheme getModulationSchema() {
        return modulationSchema;
    }

    public long getToneMap() {
        return toneMap;
    }

    public G3NodeTxModulation getModulation() {
        return modulation;
    }

    public int getTxGain() {
        return txGain;
    }

    public int getTxRes() {
        return txRes;
    }

    public int getTxCoeff() {
        return txCoeff;
    }

    public int getLqi() {
        return lqi;
    }

    public G3NodePhaseInfo getPhaseDifferential() {
        return phaseDifferential;
    }

    public int getTmrValidTime() {
        return tmrValidTime;
    }

    public int getNeighbourValidTime() {
        return neighbourValidTime;
    }
}