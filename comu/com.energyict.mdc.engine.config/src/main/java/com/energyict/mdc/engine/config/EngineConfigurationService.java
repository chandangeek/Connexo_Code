package com.energyict.mdc.engine.config;

import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;

import com.elster.jupiter.time.TimeDuration;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;

public interface EngineConfigurationService {

    String COMPONENT_NAME = "MDC";

    public Optional<ComServer> findComServer(String name);

    /**
     * Finds the ComServer with the specified unique identifier.
     *
     * @param id the ComServer id
     * @return The ComServer or <code>null</code> if no such ComServer exists
     */
    public Optional<ComServer> findComServer(long id);

    /**
     * Find all ComServers including the ones that were made obsolete.
     *
     * @return All the ComServers, including the obsolete ones
     */
    public Finder<ComServer> findAllComServers();

    /**
     * Finds the ComServer with the name of the system that is currently running this software.
     *
     * @return the ComServer or <code>null</code> if no such ComServer exists
     */
    public Optional<ComServer> findComServerBySystemName ();

    /**
     * Finds all {@link com.energyict.mdc.engine.config.OnlineComServer}s.
     *
     * @return a List of OnlineComServer
     */
    public List<OnlineComServer> findAllOnlineComServers ();

    /**
     * Finds all {@link RemoteComServer}s.
     *
     * @return a List of RemoteComServer
     */
    public List<RemoteComServer> findAllRemoteComServers ();

    /**
     * Finds all {@link RemoteComServer}s that rely on the {@link OnlineComServer}.
     *
     * @param onlineComServer The OnlineComServer
     * @return a List of {@link} RemoteComServer remoteComServers}
     */
    public List<RemoteComServer> findRemoteComServersForOnlineComServer(OnlineComServer onlineComServer);

    /**
     * Finds all {@link OfflineComServer}s.
     *
     * @return a List of OfflineComServer
     */
    public List<OfflineComServer> findAllOfflineComServers ();

    public OnlineComServer newOnlineComServerInstance();
    public OfflineComServer newOfflineComServerInstance();
    public RemoteComServer newRemoteComServerInstance();

    /**
     * Finds the ComPort with the specified unique identifier.
     *
     * @param id the ComPort id
     * @return The ComPort
     */
    public Optional<? extends ComPort> findComPort(long id);

    /**
     * Finds all the {@link ComPort}s that are owned by the specified {@link ComServer}.
     *
     * @param comServer The ComServer
     * @return The ComPorts owned by the ComServer
     */
    public List<ComPort> findComPortsByComServer(ComServer comServer);

    /**
     * Finds all {@link OutboundComPort}s.
     *
     * @return Llist of OutboundComPort
     */
    public List<OutboundComPort> findAllOutboundComPorts();

    /**
     * Finds all {@link InboundComPort}s.
     *
     * @return List of InboundComPort
     */
    public List<InboundComPort> findAllInboundComPorts();

    /**
     * Finds the {@link ComPortPool} with the specified unique identifier.
     *
     * @param id The unique identifier
     * @return The ComPortPool
     */
    public Optional<? extends ComPortPool> findComPortPool (long id);

    /**
     * Finds the {@link InboundComPortPool} with the specified unique identifier.
     *
     * @param id The unique identifier
     * @return The InboundComPortPool or <code>null</code> if no such ComPortPool exists
     *         or if the ComPortPool with that unique identifier is not inbound
     */
    public Optional<InboundComPortPool> findInboundComPortPool (long id);

    /**
     * Finds the {@link OutboundComPortPool} with the specified unique identifier.
     *
     * @param id The unique identifier
     * @return The OutboundComPortPool or <code>null</code> if no such ComPortPool exists
     *         or if the ComPortPool with that unique identifier is not outbound
     */
    public Optional<OutboundComPortPool> findOutboundComPortPool (long id);

    /**
     * Finds all the {@link OutboundComPortPool}s that can contain {@link ComPort}s
     * of the specified {@link ComPortType}.
     *
     * @param comPortType The ComPortType
     * @return The List of OutboundComPortPool
     */
    public List<OutboundComPortPool> findOutboundComPortPoolsByType(ComPortType comPortType);

    /**
     * Finds all the {@link InboundComPortPool}s that can contain {@link ComPort}s
     * of the specified {@link ComPortType}.
     *
     * @param comPortType The ComPortType
     * @return The List of InboundComPortPool
     */
    public List<InboundComPortPool> findInboundComPortPoolsByType(ComPortType comPortType);

    /**
     * Finds the {@link ComPortPool} with the specified unique name.
     *
     * @param name The unique name
     * @return The ComPortPool or <code>null</code> if no such ComPortPool exists
     */
    public Optional<? extends ComPortPool> findComPortPoolByName(String name);

    /**
     * Finds the {@link OutboundComPortPool} with the specified unique name.
     *
     * @param name The unique name
     * @return The OutboundComPortPool
     */
    public Optional<OutboundComPortPool> findOutboundComPortPoolByName (String name);

    /**
     * Finds the {@link InboundComPortPool} with the specified unique name.
     *
     * @param name The unique name
     * @return The InboundComPortPool
     */
    public Optional<InboundComPortPool> findInboundComPortPoolByName (String name);

    /**
     * Finds all the {@link InboundComPortPool}s that are using the specified.
     *
     * @param pluggableClass The discovery protocol pluggable class
     * @return The InboundComPortPools that are using the discovery protocol pluggable class
     */
    public List<InboundComPortPool> findComPortPoolByDiscoveryProtocol(PluggableClass pluggableClass);

    /**
     * Creates a new {@link InboundComPortPool} from the specifications.
     *
     * @return The newly created InboundComPortPool
     */
    public InboundComPortPool newInboundComPortPool (String name, ComPortType comPortType, InboundDeviceProtocolPluggableClass discoveryProtocol);

    /**
     * Creates a new {@link OutboundComPortPool} from the specifications.
     *
     * @return The newly created OutboundComPortPool
     */
    public OutboundComPortPool newOutboundComPortPool (String name, ComPortType comPortType, TimeDuration taskExecutionTimeout);

    List<OutboundComPortPool> findContainingComPortPoolsForComPort(OutboundComPort comPort);

    List<ComPortPool> findContainingComPortPoolsForComServer(ComServer comServer);

    List<ComPortPool> findAllComPortPools();

    List<InboundComPort> findInboundInPool(InboundComPortPool comPortPool);

    List<ComPort> findAllComPortsWithDeleted();

    ComServer parseComServerQueryResult(JSONObject comServerJSon);

    ComPort parseComPortQueryResult(JSONObject comPortJSon);

}