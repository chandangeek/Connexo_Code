package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.EventType;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.store.ComSessionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateInboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.InboundJobExecutionDataProcessor;
import com.energyict.mdc.engine.impl.core.InboundJobExecutionGroup;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.UnknownInboundDeviceEvent;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;
import com.energyict.mdc.protocol.api.inbound.FindMultipleDevices;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.ComSessionBuilder;

import java.util.List;

/**
 * Provides an implementation for the inbound communication process,
 * covering all of the validation aspect (i.e. when is communication allowed)
 * and the delegation to an actual {@link DeviceProtocol}
 * if that is necessary.
 * It is assumed that the {@link InboundDeviceProtocol} that is provided
 * has already been initialized, i.e. the proper init methods has already been called.
 * Which method needs to be called will depend on how the communication data
 * is obtained {@link InboundDeviceProtocol.InputDataType}.
 * All events, either success or failure is communicated to the Device via the
 * {@link InboundDeviceProtocol#provideResponse(InboundDeviceProtocol.DiscoverResponseType)
 * InboundDeviceProtocol.provideResponse}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (14:11)
 */
public class InboundCommunicationHandler {

    private final ServiceProvider serviceProvider;

    private InboundComPort comPort;
    private ComServerDAO comServerDAO;
    private DeviceCommandExecutor deviceCommandExecutor;
    private List<ComTaskExecution> deviceComTaskExecutions;
    private InboundConnectionTask connectionTask;
    private InboundDiscoveryContextImpl context;
    private InboundDeviceProtocol.DiscoverResponseType responseType;

    public InboundCommunicationHandler(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super();
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.serviceProvider = serviceProvider;
    }

    /**
     * Handles all of the business and technical aspects of inbound communication.
     *
     * @param inboundDeviceProtocol The InboundDeviceProtocol that will discover which {@link com.energyict.mdc.protocol.api.device.BaseDevice device}
     *                              started the inbound communication session
     * @param context               The InboundDiscoveryContext
     */
    public void handle(InboundDeviceProtocol inboundDeviceProtocol, InboundDiscoveryContextImpl context) {
        this.initializeContext(context);
        OfflineDevice device;
        InboundDeviceProtocol.DiscoverResultType discoverResultType;
        try {
            discoverResultType = this.doDiscovery(inboundDeviceProtocol);
            device = this.comServerDAO.findDevice(inboundDeviceProtocol.getDeviceIdentifier());
            if (device == null) {
                this.handleUnknownDevice(inboundDeviceProtocol);
            } else {
                this.handleKnownDevice(inboundDeviceProtocol, context, discoverResultType, device);
            }
        } catch (NotFoundException e) {
            // Device that is identified by the identifier that was discovered does not exist
            this.handleUnknownDevice(inboundDeviceProtocol);
        } catch (DuplicateException e) {
            // the findDevice on DeviceIdentifier resulted in multiple devices
            this.handleDuplicateDevicesFound(inboundDeviceProtocol, e);
        } catch (CommunicationException e) {
            this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.FAILURE);
        }
        this.closeContext();
    }

    /**
     * For each Device we found, we will create a failing InboundComSession so it is clear the the
     * connection was not properly setup because we don't know which endDevice to use.
     *
     * @param inboundDeviceProtocol the inboundDeviceProtocol
     * @param e the DuplicateException
     */
    private void handleDuplicateDevicesFound(InboundDeviceProtocol inboundDeviceProtocol, DuplicateException e) {
        if (FindMultipleDevices.class.isAssignableFrom(inboundDeviceProtocol.getDeviceIdentifier().getClass())) {
            FindMultipleDevices<?> multipleDevicesFinder = (FindMultipleDevices<?>) inboundDeviceProtocol.getDeviceIdentifier();
            List<? extends BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister>> allDevices = multipleDevicesFinder.getAllDevices();
            for (BaseDevice<?,?,?> device : allDevices) {
                if (deviceIsReadyForInboundCommunicationOnThisPort(new OfflineDeviceImpl((Device) device, new DeviceOfflineFlags()))) {
                    List<DeviceCommandExecutionToken> tokens = this.deviceCommandExecutor.tryAcquireTokens(1);
                    if (!tokens.isEmpty() && this.connectionTask != null) {
                        CompositeDeviceCommand storeCommand = new ComSessionRootDeviceCommand();
                        storeCommand.add(createFailedInboundComSessionForDuplicateDevice(e));
                        this.deviceCommandExecutor.execute(storeCommand, tokens.get(0));
                    } else {
                        this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.SERVER_BUSY);
                    }
                }
            }
        }
        this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.DEVICE_NOT_FOUND);
    }

    /**
     * This will create an {@link CreateInboundComSession} which holds a ComSessionShadow with successIndicator
     * {@link ComSession.SuccessIndicator#SetupError} and a ComSessionJournalEntryShadow which
     * contains the detailed exception.
     *
     * @param e the duplicateException
     * @return the CreateInboundComSession
     */
    private CreateInboundComSession createFailedInboundComSessionForDuplicateDevice(DuplicateException e){
        ComSessionBuilder comSessionBuilder = serviceProvider.taskHistoryService().buildComSession(connectionTask, comPort.getComPortPool(), comPort, serviceProvider.clock().now())
                .addJournalEntry(serviceProvider.clock().now(), e.getMessage(), e);
        return new CreateInboundComSession(getComPort(), this.connectionTask, comSessionBuilder, ComSession.SuccessIndicator.SetupError, serviceProvider.clock());
    }

    private void handleUnknownDevice(InboundDeviceProtocol inboundDeviceProtocol) {
        List<InboundDeviceProtocolPluggableClass> classes = serviceProvider.protocolPluggableService().findInboundDeviceProtocolPluggableClassByClassName(inboundDeviceProtocol.getClass().getName());
        UnknownInboundDeviceEvent event = null;
        if (!classes.isEmpty()) {
            event = new UnknownInboundDeviceEvent(this.comPort, inboundDeviceProtocol.getDeviceIdentifier(), classes.get(0));
        }
        this.comServerDAO.signalEvent(EventType.UNKNOWN_INBOUND_DEVICE.topic(), event);
        // Todo: do something for the DoS attacks?
        this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.DEVICE_NOT_FOUND);
    }

    private void handleKnownDevice(InboundDeviceProtocol inboundDeviceProtocol, InboundDiscoveryContext context, InboundDeviceProtocol.DiscoverResultType discoverResultType, OfflineDevice device) {
        Cryptographer cryptographer = context.getCryptographer();
        if (this.deviceRequiresEncryption(device) && cryptographer != null && !cryptographer.wasUsed()) {
            this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.ENCRYPTION_REQUIRED);
        } else {
            if (this.deviceIsReadyForInboundCommunicationOnThisPort(device)) {
                this.handleDeviceReadyForInboundCommunicationOnThisPort(inboundDeviceProtocol, discoverResultType, device);
            } else {
                this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.DEVICE_DOES_NOT_EXPECT_INBOUND);
            }
        }
    }

    private void handleDeviceReadyForInboundCommunicationOnThisPort(InboundDeviceProtocol inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResultType discoverResultType, OfflineDevice offlineDevice) {
        List<DeviceCommandExecutionToken> tokens = this.deviceCommandExecutor.tryAcquireTokens(1);
        if (tokens.isEmpty()) {
            this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.SERVER_BUSY);
        } else {
            DeviceCommandExecutionToken singleToken = tokens.get(0);
            this.startDeviceSessionInContext();
            this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.SUCCESS);
            if (InboundDeviceProtocol.DiscoverResultType.IDENTIFIER.equals(discoverResultType)) {
                this.handOverToDeviceProtocol(singleToken);
            } else {
                this.processCollectedData(inboundDeviceProtocol, singleToken, offlineDevice);
            }
        }
    }

    private void startDeviceSessionInContext() {
        context.buildComSession(connectionTask, comPort.getComPortPool(), comPort, serviceProvider.clock().now());
    }

    public InboundComPort getComPort() {
        return comPort;
    }

    public InboundConnectionTask getConnectionTask() {
        return connectionTask;
    }

    public List<ComTaskExecution> getDeviceComTaskExecutions() {
        return deviceComTaskExecutions;
    }

    public InboundDiscoveryContextImpl getContext() {
        return context;
    }

    private InboundDeviceProtocol.DiscoverResultType doDiscovery(InboundDeviceProtocol inboundDeviceProtocol) {
        return inboundDeviceProtocol.doDiscovery();
    }

    private void initializeContext(InboundDiscoveryContextImpl context) {
        this.context = context;
    }

    private void closeContext() {
        ComSessionBuilder sessionBuilder = context.getComSessionBuilder();
        if (InboundDeviceProtocol.DiscoverResponseType.SUCCESS.equals(this.responseType)) {
            this.markSuccessful(sessionBuilder);
        } else {
            this.markFailed(sessionBuilder, this.responseType);
        }
    }

    private ComSessionBuilder.EndedComSessionBuilder markSuccessful(ComSessionBuilder comSessionBuilder) {
        return comSessionBuilder.endSession(serviceProvider.clock().now(), ComSession.SuccessIndicator.Success);
    }

    private ComSessionBuilder.EndedComSessionBuilder markFailed(ComSessionBuilder comSessionBuilder, InboundDeviceProtocol.DiscoverResponseType reason) {
        switch (reason) {
            case SUCCESS: {
                assert false : "if-test that was supposed to verify that the discovery response type was NOT success clearly failed";
                throw CodingException.unrecognizedEnumValue(reason);
            }
            case DEVICE_DOES_NOT_EXPECT_INBOUND: {
                return comSessionBuilder.endSession(serviceProvider.clock().now(), ComSession.SuccessIndicator.SetupError);
            }
            case DEVICE_NOT_FOUND: {
                return comSessionBuilder.endSession(serviceProvider.clock().now(), ComSession.SuccessIndicator.Success);
            }
            case ENCRYPTION_REQUIRED: {
                return comSessionBuilder.endSession(serviceProvider.clock().now(), ComSession.SuccessIndicator.SetupError);
            }
            case SERVER_BUSY: {
                return comSessionBuilder.endSession(serviceProvider.clock().now(), ComSession.SuccessIndicator.Broken);
            }
            default: {
                throw CodingException.unrecognizedEnumValue(reason);
            }
        }
    }

    private void provideResponse(InboundDeviceProtocol inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType responseType) {
        inboundDeviceProtocol.provideResponse(responseType);
        this.responseType = responseType;
    }

    public InboundDeviceProtocol.DiscoverResponseType getResponseType() {
        return responseType;
    }

    private boolean deviceRequiresEncryption(OfflineDevice device) {
        // Todo: Use one of the typed properties so may need to delegate this to the protocol
        return true;
    }

    protected void handOverToDeviceProtocol(DeviceCommandExecutionToken token) {
        InboundJobExecutionGroup inboundJobExecutionGroup =
                new InboundJobExecutionGroup(
                        getComPort(),
                        comServerDAO,
                        deviceCommandExecutor,
                        getContext(), serviceProvider);
        inboundJobExecutionGroup.setToken(token);
        inboundJobExecutionGroup.setConnectionTask(this.connectionTask);
        inboundJobExecutionGroup.executeDeviceProtocol(this.deviceComTaskExecutions);
    }

    private void processCollectedData(InboundDeviceProtocol inboundDeviceProtocol, DeviceCommandExecutionToken token, OfflineDevice offlineDevice) {
        InboundJobExecutionDataProcessor inboundJobExecutionDataProcessor =
                new InboundJobExecutionDataProcessor(
                        getComPort(),
                        comServerDAO,
                        deviceCommandExecutor,
                        getContext(),
                        inboundDeviceProtocol,
                        offlineDevice, serviceProvider);
        inboundJobExecutionDataProcessor.setToken(token);
        inboundJobExecutionDataProcessor.setConnectionTask(this.connectionTask);
        inboundJobExecutionDataProcessor.executeDeviceProtocol(this.deviceComTaskExecutions);
    }

    private boolean deviceIsReadyForInboundCommunicationOnThisPort(OfflineDevice device) {
        this.deviceComTaskExecutions = this.comServerDAO.findExecutableInboundComTasks(device, this.comPort);
        if (this.deviceComTaskExecutions.isEmpty()) {
            this.connectionTask = null;
            return false;
        } else {
            // There is at most one InboundConnectionTask for every device
            this.connectionTask = (InboundConnectionTask) this.deviceComTaskExecutions.get(0).getConnectionTask();
            return true;
        }
    }
}