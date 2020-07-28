/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.ConnectionProperty;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

public class MeterConfigPingUtils {
    private static final String HOST = "host";
    private static final String PORT_NUMBER = "portNumber";
    private static final int TIMEOUT = 10000;  //10 seconds

    private final Thesaurus thesaurus;

    @Inject
    public MeterConfigPingUtils(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public Optional<ErrorMessage> ping(Device device) {
        ErrorMessage errorMessage = null;

        Optional<Pair<String, BigDecimal>> hostAndPort = device.getConnectionTasks().stream()
                .filter(connectionTask -> connectionTask instanceof OutboundConnectionTask)
                .map(connectionTask1 -> ((OutboundConnectionTask) connectionTask1))
                .filter(OutboundConnectionTask::isDefault)
                .map(this::getHostAndPort)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();

        if (!hostAndPort.isPresent()) {
            hostAndPort = device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask instanceof OutboundConnectionTask)
                    .map(connectionTask1 -> ((OutboundConnectionTask) connectionTask1))
                    .map(this::getHostAndPort)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findAny();
        }

        if (hostAndPort.isPresent()) {
            String host = hostAndPort.get().getFirst();
            BigDecimal port = hostAndPort.get().getLast();

            try {
                int intPort = port.intValueExact();
                if (intPort < 0 || intPort > 0xFFFF) {
                    errorMessage = createErrorMessage(MessageSeeds.WRONG_PORT_RANGE, port);
                } else if (!InetAddresses.isInetAddress(host) && !InternetDomainName.isValid(host)) {
                    errorMessage = createErrorMessage(MessageSeeds.UNKNOWN_HOST_EXCEPTION, host);
                } else {
                    try (Socket soc = new Socket()) {
                        soc.connect(new InetSocketAddress(host, intPort), TIMEOUT);
                    }
                }
            } catch (UnknownHostException uhe) {
                errorMessage = createErrorMessage(MessageSeeds.UNKNOWN_HOST_EXCEPTION, host);
            } catch (ArithmeticException ae) {
                errorMessage = createErrorMessage(MessageSeeds.WRONG_PORT_RANGE, port);
            } catch (SecurityException se) {
                errorMessage = createErrorMessage(MessageSeeds.SECURITY_EXCEPTION);
            } catch (SocketTimeoutException ste) {
                errorMessage = createErrorMessage(MessageSeeds.SOCKET_TIMEOUT_EXCEPTION);
            } catch (IOException ex) {
                errorMessage = createErrorMessage(MessageSeeds.PING_ERROR, ex.getLocalizedMessage());
            }
        } else {
            errorMessage = createErrorMessage(MessageSeeds.MISSING_HOST_PORT);
        }
        return Optional.ofNullable(errorMessage);
    }

    private Optional<Pair<String, BigDecimal>> getHostAndPort(OutboundConnectionTask task) {
        List<ConnectionTaskProperty> props = task.getProperties();
        Optional host = getConnectionTaskProperty(props, HOST);
        Optional port = getConnectionTaskProperty(props, PORT_NUMBER);
        if (host.isPresent() && port.isPresent()) {
            return Optional.of(Pair.of((String) host.get(), (BigDecimal) port.get()));
        } else {
            return Optional.empty();
        }
    }

    private Optional<Object> getConnectionTaskProperty(List<ConnectionTaskProperty> properties, String name) {
        return properties.stream().filter(prop -> prop.getName().equals(name)).map(ConnectionProperty::getValue).findFirst();
    }

    private ErrorMessage createErrorMessage(MessageSeeds messageSeed, Object... args) {
        return new ErrorMessage(messageSeed.translate(thesaurus, args), messageSeed.getErrorCode());
    }
}
