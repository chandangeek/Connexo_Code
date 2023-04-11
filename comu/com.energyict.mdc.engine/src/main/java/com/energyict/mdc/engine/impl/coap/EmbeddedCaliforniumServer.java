/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.coap;

import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;

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
import org.eclipse.californium.scandium.dtls.CertificateType;
import org.eclipse.californium.scandium.dtls.pskstore.AdvancedMultiPskStore;
import org.eclipse.californium.scandium.dtls.x509.NewAdvancedCertificateVerifier;
import org.eclipse.californium.scandium.dtls.x509.SingleCertificateProvider;
import org.eclipse.californium.scandium.dtls.x509.StaticNewAdvancedCertificateVerifier;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link EmbeddedCoapServer} interface
 * for the <a href="http://www.eclipse.org/californium/">Californium</a> coap server.
 */
public class EmbeddedCaliforniumServer implements EmbeddedCoapServer {

    private static final String COAP_CONFIGURATION_FILE = "com.energyict.mdc.engine.coap.configfile";
    private static final Logger LOGGER = Logger.getLogger(EmbeddedCaliforniumServer.class.getName());

    private final ShutdownFailureLogger shutdownFailureLogger;
    private CoapServer coapServer;
    private Configuration coapConfig;
    private BaseCoapResource coapResource;
    private boolean startCommandGiven = false;
    private boolean stopCommandGiven = false;

    private EmbeddedCaliforniumServer(CoapBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        DtlsConfig.register();
        CoapConfig.register();
        UdpConfig.register();
        try {
            coapConfig = createCoapConfiguration();
            coapServer = new CoapServer(coapConfig);
            coapResource = new BaseCoapResource(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
            coapServer.add(coapResource);
            for (InetAddress address : NetworkInterfacesUtil.getNetworkInterfaces()) {
                coapServer.addEndpoint(new CoapEndpoint.Builder().setConnector(createConnector(address, comPort)).build());
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace(System.err);
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        shutdownFailureLogger = new ComPortShutdownFailureLogger(comPort);
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

    private static byte[] hexStringToByteArray(String s) {
        byte data[] = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            data[i / 2] = (Integer.decode("0x" + s.charAt(i) + s.charAt(i + 1))).byteValue();
        }
        return data;
    }

    private Configuration createCoapConfiguration() {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        if (bundle != null) {
            String coapConfig = bundle.getBundleContext().getProperty(COAP_CONFIGURATION_FILE);
            if (coapConfig != null) {
                File configFile = new File(coapConfig);
                if (configFile.exists() && configFile.isFile()) {
                    return Configuration.createStandardWithFile(configFile);
                }
            }
        }
        return Configuration.getStandard();
    }

    private Connector createConnector(InetAddress address, CoapBasedInboundComPort comPort) throws IOException, GeneralSecurityException {
        if (comPort.isDtls()) {
            DtlsConnectorConfig.Builder builder = DtlsConnectorConfig.builder(coapConfig);
            builder.setAddress(new InetSocketAddress(address, comPort.getPortNumber()));
            if (comPort.isUsingSharedKeys()) {
                builder.setAdvancedPskStore(getPskStore(comPort));
                return new DTLSConnector(builder.build());
            }
            builder.setCertificateIdentityProvider(getCertificateProvider(comPort));
            builder.setAdvancedCertificateVerifier(getCertificateVerifier(comPort));
            return new DTLSConnector(builder.build());
        }
        return new UDPConnector(new InetSocketAddress(address, comPort.getPortNumber()), coapConfig);
    }

    private AdvancedMultiPskStore getPskStore(CoapBasedInboundComPort comPort) throws IOException {
        AdvancedMultiPskStore pskStore = new AdvancedMultiPskStore();
        if (comPort.isUsingSharedKeys()) {
            String keyStore = comPort.getKeyStoreSpecsFilePath();
            if (keyStore != null && !keyStore.isEmpty()) {
                loadPskStore(pskStore, new File(keyStore));
            }
        }
        return pskStore;
    }

    private SingleCertificateProvider getCertificateProvider(CoapBasedInboundComPort comPort) throws IOException, GeneralSecurityException {
        SslContextUtil.Credentials dtlsCredentials = getDtlsCredentials(comPort);
        return new SingleCertificateProvider(dtlsCredentials.getPrivateKey(), dtlsCredentials.getCertificateChain(), CertificateType.RAW_PUBLIC_KEY, CertificateType.X_509);
    }

    private SslContextUtil.Credentials getDtlsCredentials(CoapBasedInboundComPort comPort) throws IOException, GeneralSecurityException {
        return SslContextUtil.loadCredentials(comPort.getKeyStoreSpecsFilePath(), comPort.getContextPath(), comPort.getKeyStoreSpecsPassword()
                .toCharArray(), comPort.getKeyStoreSpecsPassword().toCharArray());
    }

    private NewAdvancedCertificateVerifier getCertificateVerifier(CoapBasedInboundComPort comPort) throws IOException, GeneralSecurityException {
        Certificate[] dtlsCertificates = getDtlsCertificates(comPort);
        return StaticNewAdvancedCertificateVerifier.builder().setTrustedCertificates(dtlsCertificates).setTrustAllRPKs().build();
    }

    private Certificate[] getDtlsCertificates(CoapBasedInboundComPort comPort) throws IOException, GeneralSecurityException {
        return SslContextUtil.loadTrustedCertificates(comPort.getTrustStoreSpecsFilePath(), comPort.getContextPath(), comPort.getTrustStoreSpecsPassword().toCharArray());
    }

    @Override
    public ServerProcessStatus getStatus() {
        if (startCommandGiven && !coapServer.isRunning()) {
            return ServerProcessStatus.STARTING;
        } else if (stopCommandGiven && coapServer.isRunning()) {
            return ServerProcessStatus.SHUTTINGDOWN;
        } else if (coapServer.isRunning()) {
            return ServerProcessStatus.STARTED;
        } else {
            return ServerProcessStatus.SHUTDOWN;
        }
    }

    @Override
    public void start() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            coapServer.start();
            startCommandGiven = true;
        } catch (Exception e) {
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
        WebSocketEventPublisherFactory webSocketEventPublisherFactory();
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

    private void loadPskStore(AdvancedMultiPskStore pskStore, File keyStoreFile) throws IOException {
        if (keyStoreFile.exists() && keyStoreFile.isFile()) {
            try (FileReader fr = new FileReader(keyStoreFile)) {
                Properties properties = new Properties();
                properties.load(fr);
                for (String key : properties.stringPropertyNames()) {
                    byte[] keyBytes = hexStringToByteArray(properties.getProperty(key));
                    pskStore.setKey(key, keyBytes);
                    //log
                }
            }
        }
    }
}