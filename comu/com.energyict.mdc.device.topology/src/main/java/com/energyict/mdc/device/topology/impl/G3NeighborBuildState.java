/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;

import java.util.Optional;

/**
* Models the state of a {@link TopologyServiceImpl.G3NeighborBuilderImpl}.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-12-16 (16:54)
*/
enum G3NeighborBuildState {
    CREATE {
        @Override
        protected void startEditing(TopologyServiceImpl.G3NeighborBuilderImpl g3NeighborBuilder, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
            // Already editing
        }

        @Override
        public void setTxGain(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txGain) {
            builder.setTxGain(txGain);
        }

        @Override
        public void setTxResolution(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txResolution) {
            builder.setTxResolution(txResolution);
        }

        @Override
        public void setTxCoefficient(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txCoefficient) {
            builder.setTxCoefficient(txCoefficient);
        }

        @Override
        public void setLinkQualityIndicator(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkQualityIndicator) {
            builder.setLinkQualityIndicator(linkQualityIndicator);
        }

        @Override
        public void setTimeToLiveFromSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds) {
            builder.setTimeToLiveFromSeconds(seconds);
        }

        @Override
        public void setToneMap(TopologyServiceImpl.G3NeighborBuilderImpl builder, long toneMap) {
            builder.setToneMap(toneMap);
        }

        @Override
        public void setToneMapTimeToLiveSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds) {
            builder.setToneMapTimeToLiveFromSeconds(seconds);
        }

        @Override
        Optional<G3Neighbor> complete(G3NeighborImpl neighborTableEntry, Optional<G3NeighborImpl> oldNeighborTableEntry) {
            oldNeighborTableEntry.ifPresent(PLCNeighborImpl::save); // The old entry was already terminated when edit mode was started
            neighborTableEntry.save();
            return Optional.of(neighborTableEntry);
        }
    },
    UPDATE {
        @Override
        protected void startEditing(TopologyServiceImpl.G3NeighborBuilderImpl g3NeighborBuilder, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
            // Already editing
        }

        @Override
        public void setTxGain(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txGain) {
            builder.terminateOldAndStartNew();
            builder.setTxGain(txGain);
        }

        @Override
        public void setTxResolution(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txResolution) {
            builder.terminateOldAndStartNew();
            builder.setTxResolution(txResolution);
        }

        @Override
        public void setTxCoefficient(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txCoefficient) {
            builder.terminateOldAndStartNew();
            builder.setTxCoefficient(txCoefficient);
        }

        @Override
        public void setLinkQualityIndicator(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkQualityIndicator) {
            builder.terminateOldAndStartNew();
            builder.setLinkQualityIndicator(linkQualityIndicator);
        }

        @Override
        public void setTimeToLiveFromSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds) {
            builder.terminateOldAndStartNew();
            builder.setTimeToLiveFromSeconds(seconds);
        }

        @Override
        public void setToneMap(TopologyServiceImpl.G3NeighborBuilderImpl builder, long toneMap) {
            builder.terminateOldAndStartNew();
            builder.setToneMap(toneMap);
        }

        @Override
        public void setToneMapTimeToLiveSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds) {
            builder.terminateOldAndStartNew();
            builder.setToneMapTimeToLiveFromSeconds(seconds);
        }

        @Override
        Optional<G3Neighbor> complete(G3NeighborImpl neighborTableEntry, Optional<G3NeighborImpl> oldNeighborTableEntry) {
            oldNeighborTableEntry.ifPresent(PLCNeighborImpl::save); // The old entry was already terminated when edit mode was started
            neighborTableEntry.save();
            return Optional.of(neighborTableEntry);
        }
    },
    TERMINATE {
        @Override
        protected void startEditing(TopologyServiceImpl.G3NeighborBuilderImpl builder, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
            builder.prepareForUpdateOrTerminateOldAndStartNew(modulationScheme, modulation, phaseInfo);
        }

        @Override
        public void setTxGain(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txGain) {
            throw illegalStateException();
        }

        @Override
        public void setTxResolution(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txResolution) {
            throw illegalStateException();
        }

        @Override
        public void setTxCoefficient(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txCoefficient) {
            throw illegalStateException();
        }

        @Override
        public void setLinkQualityIndicator(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkQualityIndicator) {
            throw illegalStateException();
        }

        @Override
        public void setTimeToLiveFromSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds) {
            throw illegalStateException();
        }

        @Override
        public void setToneMap(TopologyServiceImpl.G3NeighborBuilderImpl builder, long toneMap) {
            throw illegalStateException();
        }

        @Override
        public void setToneMapTimeToLiveSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds) {
            throw illegalStateException();
        }

        @Override
        Optional<G3Neighbor> complete(G3NeighborImpl neighborTableEntry, Optional<G3NeighborImpl> oldNeighborTableEntry) {
            neighborTableEntry.terminate();
            neighborTableEntry.save();
            return Optional.empty();
        }

        private IllegalStateException illegalStateException() {
            return new IllegalStateException("Neighbor entry building process was not switched to edit mode");
        }
    },
    COMPLETE {
        @Override
        protected void startEditing(TopologyServiceImpl.G3NeighborBuilderImpl g3NeighborBuilder, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
            throw illegalStateException();
        }

        @Override
        public void setTxGain(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txGain) {
            throw illegalStateException();
        }

        @Override
        public void setTxResolution(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txResolution) {
            throw illegalStateException();
        }

        @Override
        public void setTxCoefficient(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txCoefficient) {
            throw illegalStateException();
        }

        @Override
        public void setLinkQualityIndicator(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkQualityIndicator) {
            throw illegalStateException();
        }

        @Override
        public void setTimeToLiveFromSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds) {
            throw illegalStateException();
        }

        @Override
        public void setToneMap(TopologyServiceImpl.G3NeighborBuilderImpl builder, long toneMap) {
            throw illegalStateException();
        }

        @Override
        public void setToneMapTimeToLiveSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds) {
            throw illegalStateException();
        }

        @Override
        Optional<G3Neighbor> complete(G3NeighborImpl neighborTableEntry, Optional<G3NeighborImpl> oldNeighborTableEntry) {
            throw illegalStateException();
        }

        private IllegalStateException illegalStateException() {
            return new IllegalStateException("Neighbor entry building process is already complete");
        }
    };

    abstract Optional<G3Neighbor> complete(G3NeighborImpl neighborTableEntry, Optional<G3NeighborImpl> oldNeighborTableEntry);

    abstract void startEditing(TopologyServiceImpl.G3NeighborBuilderImpl g3NeighborBuilder, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo);

    abstract void setTxGain(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txGain);

    abstract void setTxResolution(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txResolution);

    abstract void setTxCoefficient(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txCoefficient);

    abstract void setLinkQualityIndicator(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkQualityIndicator);

    abstract void setTimeToLiveFromSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds);

    abstract void setToneMap(TopologyServiceImpl.G3NeighborBuilderImpl builder, long toneMap);

    abstract void setToneMapTimeToLiveSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds);

}