package com.energyict.protocolimplv2.dlms.idis.hs3300;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;
import com.energyict.protocol.FrameCounterCache;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class HS3300Cache extends DLMSCache implements DeviceProtocolCache, FrameCounterCache, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates whether this concerns a connection to a mirror.
     */
    private boolean connectionToBeaconMirror;

    /**
     * Object list for the mirror per client.
     */
    private Map<Integer, UniversalObject[]> mirrorObjectList = new HashMap<>();

    /**
     * Object list for the gateway per client.
     */
    private Map<Integer, UniversalObject[]> gatewayObjectList = new HashMap<>();

    /**
     * Indicates whether or not the cache has changed.
     */
    private volatile boolean changed;

    /**
     * {@link Map} containing the frame counters for the gateway.
     */
    private Map<Integer, Long> frameCountersGateway = new HashMap<>();

    /**
     * {@link Map} containing the frame counters for the mirror.
     */
    private Map<Integer, Long> frameCountersMirror = new HashMap<>();

    // For serialization
    public HS3300Cache() { }

    /**
     * Create a new instance.
     *
     * @param connectionToBeaconMirror Whether or not we're using a mirror.
     */
    public HS3300Cache(boolean connectionToBeaconMirror) {
        this.connectionToBeaconMirror = connectionToBeaconMirror;
    }

    /**
     * Indicate the cache has changed.
     *
     * @param changed <code>true</code> if the cache has changed, <code>false</code> if not.
     */
    public final void setChanged(final boolean changed) {
        this.changed = changed;
    }

    /**
     * Indicates whether or not this is a mirror connection.
     *
     * @return <code>true</code> if it is a mirror connection,
     */
    public final boolean isConnectionToBeaconMirror() {
        return this.connectionToBeaconMirror;
    }

    /**
     * Indicate whether or not this concerns a connection to a mirror.
     *
     * @param connectionToBeaconMirror <code>true</code> if this is a connection to a mirror, <code>false</code> if not.
     */
    public final void setConnectionToBeaconMirror(final boolean connectionToBeaconMirror) {
        this.connectionToBeaconMirror = connectionToBeaconMirror;
    }

    @Override
    public void setTXFrameCounter(int clientId, long frameCounter) {
        if (isConnectionToBeaconMirror()) {
            frameCountersMirror.put(clientId, frameCounter);
        } else {
            frameCountersGateway.put(clientId, frameCounter);
        }
        setChanged(true);
    }

    @Override
    public long getTXFrameCounter(int clientId) {
        if (isConnectionToBeaconMirror()) {
            if (frameCountersMirror.containsKey(clientId)) {
                return frameCountersMirror.get(clientId);
            } else {
                return -1;
            }
        } else {
            if (frameCountersGateway.containsKey(clientId)) {
                return frameCountersGateway.get(clientId);
            } else {
                return -1;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean contentChanged() {
        return this.changed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setContentChanged(final boolean changed) {
        this.changed = changed;
    }

    /**
     * Sets the object list.
     *
     * @param client     The client.
     * @param objectList The object list.
     */
    public final void setObjectList(final int client, final UniversalObject[] objectList) {
        if (this.connectionToBeaconMirror) {
            this.mirrorObjectList.put(client, objectList);
        } else {
            this.gatewayObjectList.put(client, objectList);
        }
        setChanged(true);
    }

    /**
     * Returns the object list for the particular client.
     *
     * @param client The client.
     * @return The object list, null if there is none.
     */
    public final UniversalObject[] getObjectList(final int client) {
        if (this.connectionToBeaconMirror) {
            return this.mirrorObjectList.get(client);
        } else {
            return this.gatewayObjectList.get(client);
        }
    }

}

