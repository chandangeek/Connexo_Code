package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceEventTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceCommandFactoryTest {


    @Mock
    private MeteringService meteringService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    private DataModel dataModel;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private HeadEndInterface headEndInterface;
    @Mock
    private EndDeviceControlType endDeviceControlType;
    @Mock
    private EndDevice endDevice;
    @Mock
    private PropertySpecService propertySpecService;


    private CommandFactory commandFactory;


    private Device device = mock(Device.class, Mockito.RETURNS_DEEP_STUBS);

    @Before
    public void setup() {
        commandFactory = new CommandFactoryImpl(meteringService, deviceService, deviceMessageSpecificationService, nlsService, thesaurus, propertySpecService);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));
        when(device.getDeviceConfiguration().getDeviceType().getId()).thenReturn(6L);
        when(meteringService.getEndDeviceControlType(anyString())).thenReturn(Optional.of(endDeviceControlType));
        DeviceMessageSpec message = mock(DeviceMessageSpec.class);
        when(deviceMessageSpecificationService.findMessageSpecById(anyLong())).thenReturn(Optional.of(message));
    }


    @Test
    public void createArmCommand() {
        // ARM FOR OPEN WITH ACTIVATION DATE
        String armForOpenWithActivationDateCode = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.RCDSWITCH)
                .subDomain(EndDeviceSubDomain.ACTIVATION)
                .eventOrAction(EndDeviceEventOrAction.ARMFOROPEN)
                .toCode();
        endDeviceControlType = meteringService.createEndDeviceControlType(armForOpenWithActivationDateCode);
        EndDeviceCommand armForOpenWithActivationDateCommand = commandFactory.createArmCommand(endDevice, true, Instant
                .now());
        List<DeviceMessageId> armForOpenWithActivationDateMsg = new ArrayList<>(
                Arrays.asList(
                        DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE,
                        DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE
                ));
        List<DeviceMessageId> armForOpenWithActivationDateCmdMsg = new ArrayList<>();
        armForOpenWithActivationDateCommand.getDeviceMessageIds()
                .stream()
                .forEach(id -> armForOpenWithActivationDateCmdMsg.add(DeviceMessageId.havingId(id)));
        assertTrue(armForOpenWithActivationDateCmdMsg.equals(armForOpenWithActivationDateMsg));
        //assertTrue(!armForOpenWithActivationDateCommand.getAttributes().isEmpty());
        //assertTrue(((Date) (armForOpenWithActivationDateCommand.getAttributes()
               // .get("ContactorDeviceMessage.activationdate"))).before(new Date()));

        // ARM FOR OPEN WITHOUT ACTIVATION DATE
        String armForOpenWithoutActivationDateCode = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.RCDSWITCH)
                .subDomain(EndDeviceSubDomain.NA)
                .eventOrAction(EndDeviceEventOrAction.ARMFOROPEN)
                .toCode();

        endDeviceControlType = meteringService.createEndDeviceControlType(armForOpenWithoutActivationDateCode);
        EndDeviceCommand armForOpenWithoutActivationDateCommand = commandFactory.createArmCommand(endDevice, true, null);
        List<DeviceMessageId> armForOpenWithOutActivationDateMsg = new ArrayList<>(
                Arrays.asList(
                        DeviceMessageId.CONTACTOR_ARM,
                        DeviceMessageId.CONTACTOR_OPEN
                ));
        List<DeviceMessageId> armForOpenWithOutActivationDateCmdMsg = new ArrayList<>();
        armForOpenWithoutActivationDateCommand.getDeviceMessageIds()
                .stream()
                .forEach(id -> armForOpenWithOutActivationDateCmdMsg.add(DeviceMessageId.havingId(id)));
       // assertTrue(armForOpenWithOutActivationDateCmdMsg.equals(armForOpenWithOutActivationDateMsg));
        //assertTrue(armForOpenWithoutActivationDateCommand.getAttributes().isEmpty());

        // ARM FOR CLOSE WITH ACTIVATION DATE
        String armForClosureWithActivationDateCode = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.RCDSWITCH)
                .subDomain(EndDeviceSubDomain.ACTIVATION)
                .eventOrAction(EndDeviceEventOrAction.ARMFORCLOSURE)
                .toCode();

        endDeviceControlType = meteringService.createEndDeviceControlType(armForClosureWithActivationDateCode);
        EndDeviceCommand armForClosureWithActivationDateCommand = commandFactory.createArmCommand(endDevice, false, Instant
                .now());
        List<DeviceMessageId> armForClosureWithActivationDateMsg = new ArrayList<>(
                Arrays.asList(
                        DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE,
                        DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE
                ));
        List<DeviceMessageId> armForClosureWithActivationDateCmdMsg = new ArrayList<>();
        armForClosureWithActivationDateCommand.getDeviceMessageIds()
                .stream()
                .forEach(id -> armForClosureWithActivationDateCmdMsg.add(DeviceMessageId.havingId(id)));
        assertTrue(armForClosureWithActivationDateCmdMsg.equals(armForClosureWithActivationDateMsg));
        //assertTrue(!armForClosureWithActivationDateCommand.getAttributes().isEmpty());
        //assertTrue(((Date) (armForClosureWithActivationDateCommand.getAttributes()
         //       .get("ContactorDeviceMessage.activationdate"))).before(new Date()));


        // ARM FOR CLOSE WITHOUT ACTIVATION DATE
        String armForClosureWithoutActivationDateCode = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.RCDSWITCH)
                .subDomain(EndDeviceSubDomain.NA)
                .eventOrAction(EndDeviceEventOrAction.ARMFORCLOSURE)
                .toCode();

        endDeviceControlType = meteringService.createEndDeviceControlType(armForClosureWithoutActivationDateCode);
        EndDeviceCommand armForClosureWithoutActivationDateCommand = commandFactory.createArmCommand(endDevice, false, null);
        List<DeviceMessageId> armForClosureWithoutActivationDateMsg = new ArrayList<>(
                Arrays.asList(
                        DeviceMessageId.CONTACTOR_ARM,
                        DeviceMessageId.CONTACTOR_CLOSE
                ));
        List<DeviceMessageId> armForClosureWithoutActivationDateCmdMsg = new ArrayList<>();
        armForClosureWithoutActivationDateCommand.getDeviceMessageIds()
                .stream()
                .forEach(id -> armForClosureWithoutActivationDateCmdMsg.add(DeviceMessageId.havingId(id)));
        //assertTrue(armForClosureWithoutActivationDateCmdMsg.equals(armForClosureWithoutActivationDateMsg));
        //assertTrue(armForClosureWithoutActivationDateCommand.getAttributes().isEmpty());
    }


    @Test
    public void createConnectCommand() {
        // CONNECT WITH ACTIVATION DATE
        String connectWithActivationDateCode = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.RCDSWITCH)
                .subDomain(EndDeviceSubDomain.ACTIVATION)
                .eventOrAction(EndDeviceEventOrAction.CLOSE)
                .toCode();

        endDeviceControlType = meteringService.createEndDeviceControlType(connectWithActivationDateCode);
        EndDeviceCommand connectWithActivationDateCommand = commandFactory.createConnectCommand(endDevice, Instant.now());
        List<DeviceMessageId> connectWithActivationDateMsg = new ArrayList<>(
                Arrays.asList(
                        DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE
                ));
        List<DeviceMessageId> connectWithActivationDateCmdMsg = new ArrayList<>();
        connectWithActivationDateCommand.getDeviceMessageIds()
                .stream()
                .forEach(id -> connectWithActivationDateCmdMsg.add(DeviceMessageId.havingId(id)));
        assertTrue(connectWithActivationDateCmdMsg.equals(connectWithActivationDateMsg));
        //assertTrue(!connectWithActivationDateCommand.getAttributes().isEmpty());
        //assertTrue(((Date) (connectWithActivationDateCommand.getAttributes()
         //       .get("ContactorDeviceMessage.activationdate"))).before(new Date()));


        // CONNECT WITHOUT ACTIVATION DATE
        String connectWithOutActivationDateCode = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.RCDSWITCH)
                .subDomain(EndDeviceSubDomain.NA)
                .eventOrAction(EndDeviceEventOrAction.CLOSE)
                .toCode();

        endDeviceControlType = meteringService.createEndDeviceControlType(connectWithOutActivationDateCode);
        EndDeviceCommand connectWithOutActivationDateCommand = commandFactory.createConnectCommand(endDevice, null);
        List<DeviceMessageId> armForClosureWithoutActivationDateMsg = new ArrayList<>(
                Arrays.asList(
                        DeviceMessageId.CONTACTOR_CLOSE
                ));
        List<DeviceMessageId> connectWithOutActivationDateCmdMsg = new ArrayList<>();
        connectWithOutActivationDateCommand.getDeviceMessageIds()
                .stream()
                .forEach(id -> connectWithOutActivationDateCmdMsg.add(DeviceMessageId.havingId(id)));
       // assertTrue(connectWithOutActivationDateCmdMsg.equals(armForClosureWithoutActivationDateMsg));
       // assertTrue(connectWithOutActivationDateCommand.getAttributes().isEmpty());
    }


    @Test
    public void createDisconnectCommand() {
        // DISCONNECT WITH ACTIVATION DATE
        String connectWithActivationDateCode = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.RCDSWITCH)
                .subDomain(EndDeviceSubDomain.ACTIVATION)
                .eventOrAction(EndDeviceEventOrAction.OPEN)
                .toCode();

        endDeviceControlType = meteringService.createEndDeviceControlType(connectWithActivationDateCode);
        EndDeviceCommand disconnectWithActivationDateCommand = commandFactory.createDisconnectCommand(endDevice, Instant.now());
        List<DeviceMessageId> disconnectWithActivationDateMsg = new ArrayList<>(
                Arrays.asList(
                        DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE
                ));
        List<DeviceMessageId> disconnectWithActivationDateCmdMsg = new ArrayList<>();
        disconnectWithActivationDateCommand.getDeviceMessageIds()
                .stream()
                .forEach(id -> disconnectWithActivationDateCmdMsg.add(DeviceMessageId.havingId(id)));
        assertTrue(disconnectWithActivationDateCmdMsg.equals(disconnectWithActivationDateMsg));
       // assertTrue(!disconnectWithActivationDateCommand.getAttributes().isEmpty());
       // assertTrue(((Date) (disconnectWithActivationDateCommand.getAttributes()
        //        .get("ContactorDeviceMessage.activationdate"))).before(new Date()));


        // DISCONNECT WITHOUT ACTIVATION DATE
        String disconnectWithOutActivationDateCode = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.RCDSWITCH)
                .subDomain(EndDeviceSubDomain.NA)
                .eventOrAction(EndDeviceEventOrAction.OPEN)
                .toCode();

        endDeviceControlType = meteringService.createEndDeviceControlType(disconnectWithOutActivationDateCode);
        EndDeviceCommand disconnectWithOutActivationDateCommand = commandFactory.createDisconnectCommand(endDevice, null);
        List<DeviceMessageId> disconnectWithOutActivationDateMsg = new ArrayList<>(
                Arrays.asList(
                        DeviceMessageId.CONTACTOR_OPEN
                ));
        List<DeviceMessageId> disconnectWithOutActivationDateCmdMsg = new ArrayList<>();
        disconnectWithOutActivationDateCommand.getDeviceMessageIds()
                .stream()
                .forEach(id -> disconnectWithOutActivationDateCmdMsg.add(DeviceMessageId.havingId(id)));
        //assertTrue(disconnectWithOutActivationDateCmdMsg.equals(disconnectWithOutActivationDateMsg));
       // assertTrue(disconnectWithOutActivationDateCommand.getAttributes().isEmpty());
    }

    @Test
    public void createEnableLoadLimitCommand(){
        String enableLoadLimitCode = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.LOADCONTROL)
                .subDomain(EndDeviceSubDomain.SUPPLYCAPACITYLIMIT)
                .eventOrAction(EndDeviceEventOrAction.LIMITREACHED)
                .toCode();

        endDeviceControlType = meteringService.createEndDeviceControlType(enableLoadLimitCode);
        Quantity limit = Quantity.create(BigDecimal.TEN, 3, "Wh");
        EndDeviceCommand enableLoadLimitCommand = commandFactory.createEnableLoadLimitCommand(endDevice, limit);
        List<DeviceMessageId> enableLoadLimitMsg = new ArrayList<>(
                Arrays.asList(
                        DeviceMessageId.LOAD_BALANCING_ENABLE_LOAD_LIMITING
                ));
        List<DeviceMessageId> enableLoadLimitCmdMsg = new ArrayList<>();
        enableLoadLimitCommand.getDeviceMessageIds()
                .stream()
                .forEach(id -> enableLoadLimitCmdMsg.add(DeviceMessageId.havingId(id)));
        assertTrue(enableLoadLimitCmdMsg.equals(enableLoadLimitMsg));
       // assertTrue(!enableLoadLimitCommand.getAttributes().isEmpty());
       // assertTrue(((Quantity) (enableLoadLimitCommand.getAttributes()
       //         .get("LoadBalanceDeviceMessage.parameters.normalthreshold"))).equals(limit));
    }

    @Test
    public void createDisableLoadLimitCommand(){
        String disableLoadLimitCode = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.LOADCONTROL)
                .subDomain(EndDeviceSubDomain.SUPPLYCAPACITYLIMIT)
                .eventOrAction(EndDeviceEventOrAction.DISABLE)
                .toCode();

        endDeviceControlType = meteringService.createEndDeviceControlType(disableLoadLimitCode);
        EndDeviceCommand disableLoadLimitCommand = commandFactory.createDisableLoadLimitCommand(endDevice);
        List<DeviceMessageId> disableLoadLimitMsg = new ArrayList<>(
                Arrays.asList(
                        DeviceMessageId.LOAD_BALANCING_DISABLE_LOAD_LIMITING
                ));
        List<DeviceMessageId> disableLoadLimitCmdMsg = new ArrayList<>();
        disableLoadLimitCommand.getDeviceMessageIds()
                .stream()
                .forEach(id -> disableLoadLimitCmdMsg.add(DeviceMessageId.havingId(id)));
       // assertTrue(disableLoadLimitCmdMsg.equals(disableLoadLimitMsg));
       // assertTrue(disableLoadLimitCommand.getAttributes().isEmpty());
    }
}
