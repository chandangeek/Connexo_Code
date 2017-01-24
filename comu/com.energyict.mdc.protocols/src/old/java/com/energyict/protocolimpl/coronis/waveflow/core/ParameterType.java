package com.energyict.protocolimpl.coronis.waveflow.core;

/**
 * This identifies which protocol the ParameterId's belong to.
 * Default is WaveFlowV2.
 * This way, we can make two parameters with the same ID and handle them differently, based on the protocol implementation
 * <p/>
 * Copyrights EnergyICT
 * Date: 4/04/13
 * Time: 17:09
 * Author: khe
 */
public enum ParameterType {
    Hydreka,
    WaveFlowV1_433MHz,
    WaveFlowV2
}