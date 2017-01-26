package com.energyict.mdc.engine.config;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface EngineConfigurationService {

    String COMPONENT_NAME = "MDC";

    Optional<ComServer> findComServer(String name);

    /**
     * Finds the ComServer with the specified unique identifier.
     *
     * @param id the ComServer id
     * @return The ComServer or <code>null</code> if no such ComServer exists
     */
    Optional<ComServer> findComServer(long id);

    Optional<ComServer> findAndLockComServerByIdAndVersion(long id, long version);

    /**
     * Find all ComServers including the ones that were made obsolete.
     *
     * @return All the ComServers, including the obsolete ones
     */
    Finder<ComServer> findAllComServers();

    /**
     * Finds the ComServer with the name of the system that is currently running this software.
     *
     * @return the ComServer or <code>null</code> if no such ComServer exists
     */
    Optional<ComServer> findComServerBySystemName();

    /**
     * Finds all {@link com.energyict.mdc.engine.config.OnlineComServer}s.
     *
     * @return a List of OnlineComServer
     */
    List<OnlineComServer> findAllOnlineComServers();

    /**
     * Finds all {@link RemoteComServer}s.
     *
     * @return a List of RemoteComServer
     */
    List<RemoteComServer> findAllRemoteComServers();

    /**
     * Finds all {@link RemoteComServer}s that rely on the {@link OnlineComServer}.
     *
     * @param onlineComServer The OnlineComServer
     * @return a List of {@link} RemoteComServer remoteComServers}
     */
    List<RemoteComServer> findRemoteComServersForOnlineComServer(OnlineComServer onlineComServer);

    /**
     * Finds all {@link OfflineComServer}s.
     *
     * @return a List of OfflineComServer
     */
    List<OfflineComServer> findAllOfflineComServers();

    /**
     * Finds the {@link ComServer} with the specified event registration uri.
     *
     * @param serverName
     * @param eventRegistrationPort
     * @return the ComServer or <code>null</code> if no such ComServer exists
     */
    Optional<ComServer> findComServerByServerNameAndEventRegistrationPort(String serverName, int eventRegistrationPort);

    /**
     * Finds the {@link ComServer} with the specified status uri.
     *
     * @param serverName
     * @param statusPort
     * @return the ComServer or <code>null</code> if no such ComServer exists
     */
    Optional<ComServer> findComServerByServerNameAndStatusPort(String serverName, int statusPort);

    OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> newOnlineComServerBuilder();

    ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> newOfflineComServerBuilder();

    RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> newRemoteComServerBuilder();

    /**
     * Finds the ComPort with the specified unique identifier.
     *
     * @param id the ComPort id
     * @return The ComPort
     */
    Optional<? extends ComPort> findComPort(long id);

    Optional<? extends ComPort> findAndLockComPortByIdAndVersion(long id, long version);

    /**
     * Finds all the {@link ComPort}s that are owned by the specified {@link ComServer}.
     *
     * @param comServer The ComServer
     * @return The ComPorts owned by the ComServer
     */
    List<ComPort> findComPortsByComServer(ComServer comServer);

    /**
     * Finds all {@link OutboundComPort}s.
     *
     * @return Llist of OutboundComPort
     */
    List<OutboundComPort> findAllOutboundComPorts();

    /**
     * Finds all {@link InboundComPort}s.
     *
     * @return List of InboundComPort
     */
    List<InboundComPort> findAllInboundComPorts();

    /**
     * Finds the {@link ComPortPool} with the specified unique identifier.
     *
     * @param id The unique identifier
     * @return The ComPortPool
     */
    Optional<? extends ComPortPool> findComPortPool(long id);

    Optional<? extends ComPortPool> findAndLockComPortPoolByIdAndVersion(long id, long version);

    /**
     * Finds the {@link InboundComPortPool} with the specified unique identifier.
     *
     * @param id The unique identifier
     * @return The InboundComPortPool or <code>null</code> if no such ComPortPool exists
     * or if the ComPortPool with that unique identifier is not inbound
     */
    Optional<InboundComPortPool> findInboundComPortPool(long id);

    /**
     * Finds the {@link OutboundComPortPool} with the specified unique identifier.
     *
     * @param id The unique identifier
     * @return The OutboundComPortPool or <code>null</code> if no such ComPortPool exists
     * or if the ComPortPool with that unique identifier is not outbound
     */
    Optional<OutboundComPortPool> findOutboundComPortPool(long id);

    /**
     * Finds all the {@link OutboundComPortPool}s that can contain {@link ComPort}s
     * of the specified {@link ComPortType}.
     *
     * @param comPortType The ComPortType
     * @return The List of OutboundComPortPool
     */
    List<OutboundComPortPool> findOutboundComPortPoolsByType(ComPortType comPortType);

    /**
     * Finds all the {@link InboundComPortPool}s that can contain {@link ComPort}s
     * of the specified {@link ComPortType}.
     *
     * @param comPortType The ComPortType
     * @return The List of InboundComPortPool
     */
    List<InboundComPortPool> findInboundComPortPoolsByType(ComPortType comPortType);

    /**
     * Finds the {@link ComPortPool} with the specified unique name.
     *
     * @param name The unique name
     * @return The ComPortPool or <code>null</code> if no such ComPortPool exists
     */
    Optional<? extends ComPortPool> findComPortPoolByName(String name);

    /**
     * Finds the {@link OutboundComPortPool} with the specified unique name.
     *
     * @param name The unique name
     * @return The OutboundComPortPool
     */
    Optional<OutboundComPortPool> findOutboundComPortPoolByName(String name);

    /**
     * Finds the {@link InboundComPortPool} with the specified unique name.
     *
     * @param name The unique name
     * @return The InboundComPortPool
     */
    Optional<InboundComPortPool> findInboundComPortPoolByName(String name);

    /**
     * Finds all the {@link InboundComPortPool}s that are using the specified.
     *
     * @param pluggableClass The discovery protocol pluggable class
     * @return The InboundComPortPools that are using the discovery protocol pluggable class
     */
    List<InboundComPortPool> findComPortPoolByDiscoveryProtocol(PluggableClass pluggableClass);

    /**
     * Creates a new {@link InboundComPortPool} from the specifications.
     *
     * @return The newly created InboundComPortPool
     */
    InboundComPortPool newInboundComPortPool(String name, ComPortType comPortType, InboundDeviceProtocolPluggableClass discoveryProtocol, Map<String, Object> properties);

    /**
     * Creates a new {@link OutboundComPortPool} from the specifications.
     *
     * @return The newly created OutboundComPortPool
     */
    OutboundComPortPool newOutboundComPortPool(String name, ComPortType comPortType, TimeDuration taskExecutionTimeout);

    List<OutboundComPortPool> findContainingComPortPoolsForComPort(OutboundComPort comPort);

    List<ComPortPool> findContainingComPortPoolsForComServer(ComServer comServer);

    List<ComPortPool> findAllComPortPools();

    List<InboundComPort> findInboundInPool(InboundComPortPool comPortPool);

    List<ComPort> findAllComPortsIncludingObsolete();

    /**
     * Locks the given ComServer. This is a blocking call until a proper lock is provided
     *
     * @param comServer the ComServer to lock
     * @return the 'locked' ComServer object
     */
    ComServer lockComServer(ComServer comServer);
}