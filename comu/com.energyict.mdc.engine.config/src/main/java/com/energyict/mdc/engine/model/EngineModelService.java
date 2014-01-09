package com.energyict.mdc.engine.model;


import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.model.impl.ServerModemBasedInboundComPort;
import com.energyict.mdc.engine.model.impl.ServerOutboundComPort;
import com.energyict.mdc.engine.model.impl.ServerServletBasedInboundComPort;
import com.energyict.mdc.engine.model.impl.ServerTCPBasedInboundComPort;
import com.energyict.mdc.engine.model.impl.ServerUDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.PluggableClass;

import java.sql.SQLException;
import java.util.List;

public interface EngineModelService {
    public ComServer findComServer(String name);
    /**
     * Finds the ComServer with the specified unique identifier.
     *
     * @param id the ComServer id
     * @return The ComServer or <code>null</code> if no such ComServer exists
     */
    public ComServer findComServer(long id);

    /**
     * Find all ComServers including the ones that were made obsolete.
     *
     * @return All the ComServers, including the obsolete ones
     */
    public List<ComServer> findAllComServers();

    /**
     * Finds the ComServer with the name of the system that is currently running this software.
     *
     * @return the ComServer or <code>null</code> if no such ComServer exists
     */
    public ComServer findComServerBySystemName ();

    /**
     * Finds all {@link com.energyict.mdc.engine.model.OnlineComServer onlineComServers}
     *
     * @return a List of all {@link com.energyict.mdc.engine.model.OnlineComServer onlineComServers}
     */
    public List<OnlineComServer> findAllOnlineComServers ();

    /**
     * Finds all {@link com.energyict.mdc.engine.model.RemoteComServer remoteComServers}
     *
     * @return a List of all {@link com.energyict.mdc.engine.model.RemoteComServer remoteComServers}
     */
    public List<RemoteComServer> findAllRemoteComServers ();

    /**
     * Returns the number of offline servers
     * @return the number of offline servers
     */
    int getOfflineServerCount();

    /**
     * Finds all {@link com.energyict.mdc.engine.model.OfflineComServer offlineComServers}
     *
     * @return a List of all {@link com.energyict.mdc.engine.model.OfflineComServer offlineComServers}
     */
    public List<OfflineComServer> findAllOfflineComServers ();

    /**
     * Finds all {@link RemoteComServer}s that rely on the {@link} OnlineComServer}.
     *
     * @param onlineComServer The OnlineComServer
     * @return a List of {@link} RemoteComServer remoteComServers}
     */
    public List<RemoteComServer> findRemoteComServersWithOnlineComServer (OnlineComServer onlineComServer);

    public OnlineComServer newOnlineComServerInstance();
    public OfflineComServer newOfflineComServerInstance();
    public RemoteComServer newRemoteComServerInstance();

    /**
     * Finds the ComPort with the specified unique identifier.
     *
     * @param id the ComPort id
     * @return The ComPort or <code>null</code> if no such ComPort exists
     */
    public ComPort findComport(int id);

    /**
     * Finds all the {@link ComPort}s that are owned by the specified {@link ComServer}.
     *
     * @param comServer The ComServer
     * @return The ComPorts owned by the ComServer
     */
    public List<ComPort> findComPortsByComServer(ComServer comServer);

    /**
     * Finds the {@link ComPort} with the specified name within the context
     * of the specified {@link ComServer}.
     *
     * @param name        The name of the ComPort
     * @return The ComPort with the specified name or <code>null</code>
     *         if no such ComPort exists.
     */
    public ComPort findComPortByNameInComServer(String name, ComServer comServer);

    /**
     * Finds all {@link OutboundComPort outboundComports}
     *
     * @return all {@link OutboundComPort outboundComports}
     */
    public List<OutboundComPort> findAllOutboundComPorts();

    /**
     * Finds all {@link InboundComPort inboundComPorts}
     *
     * @return all {@link InboundComPort inboundComPorts}
     */
    public List<InboundComPort> findAllInboundComPorts();

    public ServerOutboundComPort newOutbound(ComServer owner);

    public ServerModemBasedInboundComPort newModemBasedInbound(ComServer owner);

    public ServerTCPBasedInboundComPort newTCPBasedInbound(ComServer owner);

    public ServerUDPBasedInboundComPort newUDPBasedInbound(ComServer owner);

    public ServerServletBasedInboundComPort newServletBasedInbound(ComServer owner);

    /**
     * Finds the {@link ComPortPool} with the specified unique identifier.
     *
     * @param id The unique identifier
     * @return The ComPortPool or <code>null</code> if no such ComPortPool exists
     */
    public ComPortPool findComPortPool (int id);

    /**
     * Finds the {@link com.energyict.mdc.engine.model.InboundComPortPool} with the specified unique identifier.
     *
     * @param id The unique identifier
     * @return The InboundComPortPool or <code>null</code> if no such ComPortPool exists
     *         or if the ComPortPool with that unique identifier is not inbound
     */
    public InboundComPortPool findInboundComPortPool (int id);

    /**
     * Finds the {@link com.energyict.mdc.engine.model.OutboundComPortPool} with the specified unique identifier.
     *
     * @param id The unique identifier
     * @return The OutboundComPortPool or <code>null</code> if no such ComPortPool exists
     *         or if the ComPortPool with that unique identifier is not outbound
     */
    public OutboundComPortPool findOutboundComPortPool (int id);

    /**
     * Finds all the {@link OutboundComPortPool} that can contain {@link com.energyict.mdc.engine.model.ComPort}s
     * of the specified {@link com.energyict.mdc.protocol.api.ComPortType}.
     *
     * @param comPortType The ComPortType
     * @return The List of OutboundComPortPool
     */
    public List<OutboundComPortPool> findOutboundComPortPoolByType (ComPortType comPortType);

    /**
     * Finds the {@link ComPortPool} with the specified unique name.
     *
     * @param name The unique name
     * @return The ComPortPool or <code>null</code> if no such ComPortPool exists
     */
    public ComPortPool findComPortPool (String name);

    /**
     * Finds all the {@link InboundComPortPool}s that are using the specified
     *
     * @param pluggableClass The discovery protocol pluggable class
     * @return The InboundComPortPools that are using the discovery protocol pluggable class
     */
    public List<InboundComPortPool> findComPortPoolByDiscoveryProtocol(PluggableClass pluggableClass);

    /**
     * Creates a new {@link InboundComPortPool} from the specifications
     *
     * @return The newly created InboundComPortPool
     * @throws com.energyict.mdc.common.BusinessException Thrown when a business constraint is violated
     * @throws java.sql.SQLException Thrown when a database constraint is violated
     */
    public InboundComPortPool newInboundComPortPool ();

    /**
     * Creates a new {@link OutboundComPortPool} from the specifications
     *
     * @return The newly created OutboundComPortPool
     * @throws BusinessException Thrown when a business constraint is violated
     * @throws SQLException Thrown when a database constraint is violated
     */
    public OutboundComPortPool newOutboundComPortPool ();


    void removeComPortFromPools(ComPort comPort);

    List<ComPortPool> findContainingComPortPoolsForComPort(ComPort comPort);

    List<ComPortPool> findContainingComPortPoolsForComServer(ComServer comServer);
}
