/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.topology.*;

import java.util.Date;
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
        protected void startEditing(TopologyServiceImpl.G3NeighborBuilderImpl g3NeighborBuilder,
                                    ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
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
        public void setNodeAddress(TopologyServiceImpl.G3NeighborBuilderImpl builder, String nodeAddress) {
            builder.setNodeAddress(nodeAddress);
        }

        @Override
        void setShortAddress(TopologyServiceImpl.G3NeighborBuilderImpl builder, int shortAddress) {
            builder.setShortAddress(shortAddress);
        }

        @Override
        void setLastUpdate(TopologyServiceImpl.G3NeighborBuilderImpl builder, Date lastUpdate) {
            builder.setLastUpdate(lastUpdate);
        }

        @Override
        void setLastPathRequest(TopologyServiceImpl.G3NeighborBuilderImpl builder, Date lastPathRequest) {
            builder.setLastPathRequest(lastPathRequest);
        }

        @Override
        void setRoundTrip(TopologyServiceImpl.G3NeighborBuilderImpl builder, long roundTrip) {
            builder.setRoundTrip(roundTrip);
        }

        @Override
        void setLinkCost(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkCost) {
            builder.setLinkCost(linkCost);
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
        protected void startEditing(TopologyServiceImpl.G3NeighborBuilderImpl g3NeighborBuilder,
                                    ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
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
        void setNodeAddress(TopologyServiceImpl.G3NeighborBuilderImpl builder, String nodeAddress) {
            builder.terminateOldAndStartNew();
            builder.setNodeAddress(nodeAddress);
        }

        @Override
        void setShortAddress(TopologyServiceImpl.G3NeighborBuilderImpl builder, int shortAddress) {
            builder.terminateOldAndStartNew();
            builder.setShortAddress(shortAddress);
        }

        @Override
        void setLastUpdate(TopologyServiceImpl.G3NeighborBuilderImpl builder, Date lastUpdate) {
            builder.terminateOldAndStartNew();
            builder.setLastUpdate(lastUpdate);
        }

        @Override
        void setLastPathRequest(TopologyServiceImpl.G3NeighborBuilderImpl builder, Date lastPathRequest) {
            builder.terminateOldAndStartNew();
            builder.setLastPathRequest(lastPathRequest);
        }

        @Override
        void setRoundTrip(TopologyServiceImpl.G3NeighborBuilderImpl builder, long roundTrip) {
            builder.terminateOldAndStartNew();
            builder.setRoundTrip(roundTrip);
        }

        @Override
        void setLinkCost(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkCost) {
            builder.terminateOldAndStartNew();
            builder.setLinkCost(linkCost);
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
        protected void startEditing(TopologyServiceImpl.G3NeighborBuilderImpl builder,
                                    ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
            builder.prepareForUpdateOrTerminateOldAndStartNew(modulationScheme, modulation, phaseInfo, g3NodeState);
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
        void setNodeAddress(TopologyServiceImpl.G3NeighborBuilderImpl builder, String nodeAddress) {
            throw illegalStateException();
        }

        @Override
        void setShortAddress(TopologyServiceImpl.G3NeighborBuilderImpl builder, int shortAddress) {
            throw illegalStateException();
        }

        @Override
        void setLastUpdate(TopologyServiceImpl.G3NeighborBuilderImpl builder, Date lastUpdate) {
            throw illegalStateException();
        }

        @Override
        void setLastPathRequest(TopologyServiceImpl.G3NeighborBuilderImpl builder, Date lastPathRequest) {
            throw illegalStateException();
        }

        @Override
        void setRoundTrip(TopologyServiceImpl.G3NeighborBuilderImpl builder, long roundTrip) {
            throw illegalStateException();
        }

        @Override
        void setLinkCost(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkCost) {
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
        protected void startEditing(TopologyServiceImpl.G3NeighborBuilderImpl g3NeighborBuilder,
                                    ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
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
        void setNodeAddress(TopologyServiceImpl.G3NeighborBuilderImpl builder, String nodeAddress) {
            throw illegalStateException();
        }

        @Override
        void setShortAddress(TopologyServiceImpl.G3NeighborBuilderImpl builder, int shortAddress) {
            throw illegalStateException();
        }

        @Override
        void setLastUpdate(TopologyServiceImpl.G3NeighborBuilderImpl builder, Date lastUpdate) {
            throw illegalStateException();
        }

        @Override
        void setLastPathRequest(TopologyServiceImpl.G3NeighborBuilderImpl builder, Date lastPathRequest) {
            throw illegalStateException();
        }

        @Override
        void setRoundTrip(TopologyServiceImpl.G3NeighborBuilderImpl builder, long roundTrip) {
            throw illegalStateException();
        }

        @Override
        void setLinkCost(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkCost) {
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

    abstract void startEditing(TopologyServiceImpl.G3NeighborBuilderImpl g3NeighborBuilder, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState);

    abstract void setTxGain(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txGain);

    abstract void setTxResolution(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txResolution);

    abstract void setTxCoefficient(TopologyServiceImpl.G3NeighborBuilderImpl builder, int txCoefficient);

    abstract void setLinkQualityIndicator(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkQualityIndicator);

    abstract void setTimeToLiveFromSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds);

    abstract void setToneMap(TopologyServiceImpl.G3NeighborBuilderImpl builder, long toneMap);

    abstract void setToneMapTimeToLiveSeconds(TopologyServiceImpl.G3NeighborBuilderImpl builder, int seconds);

    abstract void setNodeAddress(TopologyServiceImpl.G3NeighborBuilderImpl builder, String nodeAddress);

    abstract void setShortAddress(TopologyServiceImpl.G3NeighborBuilderImpl builder, int shortAddress);

    abstract void setLastUpdate(TopologyServiceImpl.G3NeighborBuilderImpl builder, Date lastUpdate);

    abstract void setLastPathRequest(TopologyServiceImpl.G3NeighborBuilderImpl builder, Date lastPathRequest);

    abstract void setRoundTrip(TopologyServiceImpl.G3NeighborBuilderImpl builder, long roundTrip);

    abstract void setLinkCost(TopologyServiceImpl.G3NeighborBuilderImpl builder, int linkCost);

}