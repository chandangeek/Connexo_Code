/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.coap;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.LongFactory;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.upl.TypedProperties;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.UDPConnector;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.config.UdpConfig;
import org.eclipse.californium.elements.util.NetworkInterfacesUtil;
import org.eclipse.californium.elements.util.SslContextUtil;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConfig;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.x509.NewAdvancedCertificateVerifier;
import org.eclipse.californium.scandium.dtls.x509.SingleCertificateProvider;
import org.eclipse.californium.scandium.dtls.x509.StaticNewAdvancedCertificateVerifier;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link EmbeddedCoapServer} interface
 * for the <a href="http://www.eclipse.org/californium/">Californium</a> coap server.
 */
public class EmbeddedCaliforniumServer implements EmbeddedCoapServer {

    public static final String MAX_IDLE_TIME = "maxIdleTime";
    public static final BigDecimal MAX_IDLE_TIME_DEFAULT_VALUE = BigDecimal.valueOf(200000);
    private static final Logger LOGGER = Logger.getLogger(EmbeddedCaliforniumServer.class.getName());

    /**
     * The number of seconds that accepted requests are allowed to complete
     * during the graceful shutdown.
     */
    private static final int GRACEFUL_SHUTDOWN_SECONDS = 3;
    private final static String INBOUND_COMPORT_SERVICE = "Californium_InboundComportService";
    private final static String EVENT_MECHANISM = "Californium_EventMechanism";
    private final ShutdownFailureLogger shutdownFailureLogger;
    private CoapServer coapServer;
    private String threadPoolName;
    private boolean startCommandGiven = false;
    private boolean stopCommandGiven = false;

    private EmbeddedCaliforniumServer(ShutdownFailureLogger shutdownFailureLogger) {
        super();
        this.shutdownFailureLogger = shutdownFailureLogger;
    }

    private EmbeddedCaliforniumServer(CoapBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super();
        threadPoolName = INBOUND_COMPORT_SERVICE;
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName(threadPoolName);

        DtlsConfig.register();
        CoapConfig.register();
        UdpConfig.register();

        coapServer = new CoapServer();
        BasedCoapResource comResource = new BasedCoapResource(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        coapServer.add(comResource);

        Configuration configuration = Configuration.getStandard();
        for (InetAddress address : NetworkInterfacesUtil.getNetworkInterfaces()) {
            InetSocketAddress socketAddress = new InetSocketAddress(address, comPort.getPortNumber());
            if (comPort.isDtls()) {
                try {
                    CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
                    String keystore = comPort.getKeyStoreSpecsFilePath() + "#" + comPort.getKeyStoreSpecsPassword();
                    String keytrust = comPort.getTrustStoreSpecsFilePath() + "#" + comPort.getTrustStoreSpecsPassword();
                    builder.setConnector(createDtlsConnector(configuration, socketAddress, keystore, keytrust));
                    coapServer.addEndpoint(builder.build());
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace(System.err);
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            } else {
                CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
                builder.setConnector(createUdpConnector(configuration, socketAddress));
                coapServer.addEndpoint(builder.build());
            }
        }
        this.shutdownFailureLogger = new ComPortShutdownFailureLogger(comPort);
    }

    /**
     * Constructs a new EmbeddedCaliforniumServer that will host
     * the coapResource that supports inbound communication
     * on the specified {@link CoapBasedInboundComPort ComPort}.
     * It will use the {@link ComServerDAO} to get access to persistent data.
     * Furthermore, it will use the {@link DeviceCommandExecutor} to execute
     * commands against devices for which data was collected.
     *
     * @param comPort               The ServerCoapBasedInboundComPort
     * @param comServerDAO          The ComServerDAO
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param serviceProvider       The IssueService
     */
    public static EmbeddedCaliforniumServer newForInboundDeviceCommunication(CoapBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        return new EmbeddedCaliforniumServer(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    private static int getPortNumber(URI uri, int defaultPort) {
        int port = uri.getPort();
        if (port == -1) {
            return defaultPort;
        } else {
            return port;
        }
    }

    /**
     * Create the DTLS Connector for the endpoint.
     */
    private Connector createDtlsConnector(Configuration configuration, InetSocketAddress address, String keystore, String keytrust) throws GeneralSecurityException, IOException {
        SslContextUtil.Credentials serverCredentials = SslContextUtil.loadCredentials(keystore);
        Certificate[] serverCertificates = SslContextUtil.loadTrustedCertificates(keytrust);
        DtlsConnectorConfig.Builder builder = DtlsConnectorConfig.builder(configuration);
        builder.setAddress(address);
        SingleCertificateProvider certificate = new SingleCertificateProvider(serverCredentials.getPrivateKey(), serverCredentials.getCertificateChain());
        builder.setCertificateIdentityProvider(certificate);
        NewAdvancedCertificateVerifier trust = StaticNewAdvancedCertificateVerifier.builder()
                .setTrustedCertificates(serverCertificates).build();
        builder.setAdvancedCertificateVerifier(trust);
        DTLSConnector connector = new DTLSConnector(builder.build());
        return connector;
    }

    /**
     * Create the simple UDP Connector for the endpoint.
     */
    private Connector createUdpConnector(Configuration configuration, InetSocketAddress address) {
        Connector connector = new UDPConnector(address, configuration);
        return connector;
    }

    /**
     * Get the maxIdleTime property that is defined on the discovery protocol of the ComPort pool.
     */
    private int getMaxIdleTime(CoapBasedInboundComPort comPort) {
        BasicPropertySpec idleTime = new BasicPropertySpec(new LongFactory());
        PluggableClass discoveryProtocolPluggableClass = comPort.getComPortPool().getDiscoveryProtocolPluggableClass();
        TypedProperties properties = discoveryProtocolPluggableClass.getProperties(Arrays.asList(idleTime));
        return properties.getTypedProperty(MAX_IDLE_TIME, MAX_IDLE_TIME_DEFAULT_VALUE).intValue();
    }

    @Override
    public ServerProcessStatus getStatus() {
        if (coapServer.isRunning()) {
            return ServerProcessStatus.STARTED;
        } else if (startCommandGiven && !coapServer.isRunning()) {
            return ServerProcessStatus.STARTING;
        } else if (stopCommandGiven && coapServer.isRunning()) {
            return ServerProcessStatus.SHUTTINGDOWN;
        }
        return ServerProcessStatus.SHUTDOWN;
    }

    @Override
    public void start() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            coapServer.start();
            startCommandGiven = true;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public void shutdown() {
        this.shutdown(false);
    }

    @Override
    public void shutdownImmediate() {
        this.shutdown(true);
    }

    private void shutdown(boolean immediate) {
        startCommandGiven = false;
        stopCommandGiven = true;
        try {
            if (immediate) {
                coapServer.destroy();
            } else {
                coapServer.stop();
            }
        } catch (Exception e) {
            this.shutdownFailureLogger.log(e, Logger.getLogger(EmbeddedCaliforniumServer.class.getName()));
        }
    }

    public interface ServiceProvider {
        public WebSocketEventPublisherFactory webSocketEventPublisherFactory();
    }

    private interface ShutdownFailureLogger {
        void log(Exception e, Logger logger);
    }

    private static class ComPortShutdownFailureLogger implements ShutdownFailureLogger {
        private final CoapBasedInboundComPort comPort;


        private ComPortShutdownFailureLogger(CoapBasedInboundComPort comPort) {
            super();
            this.comPort = comPort;
        }

        @Override
        public void log(Exception e, Logger logger) {
            String message = "Embedded coap server for communication port " + this.comPort.getName() + "(" + this.comPort.getComServer().getName() + ") failed to stop";
            logger.info(message);
            logger.log(Level.FINE, message, e);
        }
    }
}