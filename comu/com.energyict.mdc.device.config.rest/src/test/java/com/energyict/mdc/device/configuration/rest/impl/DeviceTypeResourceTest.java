package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.devtools.tests.Answers;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.configuration.rest.RegisterConfigInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import org.glassfish.jersey.client.ClientResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class DeviceTypeResourceTest extends DeviceConfigurationApplicationJerseyTest {
    public static final ReadingType READING_TYPE = new ReadingType() {
        @Override
        public MacroPeriod getMacroPeriod() {
            return MacroPeriod.DAILY;
        }

        @Override
        public Aggregate getAggregate() {
            return Aggregate.AVERAGE;
        }

        @Override
        public TimeAttribute getMeasuringPeriod() {
            return TimeAttribute.MINUTE15;
        }

        @Override
        public Accumulation getAccumulation() {
            return Accumulation.BULKQUANTITY;
        }

        @Override
        public FlowDirection getFlowDirection() {
            return FlowDirection.FORWARD;
        }

        @Override
        public Commodity getCommodity() {
            return Commodity.AIR;
        }

        @Override
        public MeasurementKind getMeasurementKind() {
            return MeasurementKind.ALARM;
        }

        @Override
        public RationalNumber getInterharmonic() {
            return RationalNumber.NOTAPPLICABLE;
        }

        @Override
        public RationalNumber getArgument() {
            return RationalNumber.NOTAPPLICABLE;
        }

        @Override
        public int getTou() {
            return 1;
        }

        @Override
        public int getCpp() {
            return 1;
        }

        @Override
        public int getConsumptionTier() {
            return 1;
        }

        @Override
        public Phase getPhases() {
            return Phase.PHASES1;
        }

        @Override
        public MetricMultiplier getMultiplier() {
            return MetricMultiplier.ATTO;
        }

        @Override
        public ReadingTypeUnit getUnit() {
            return ReadingTypeUnit.AMPERE;
        }

        @Override
        public Currency getCurrency() {
            return java.util.Currency.getAvailableCurrencies().iterator().next();
        }

        @Override
        public boolean isBulkQuantityReadingType(ReadingType readingType) {
            return false;
        }

        @Override
        public boolean isRegular() {
            return false;
        }

        @Override
        public long getVersion() {
            return 1;
        }

        @Override
        public void setDescription(String description) {
        }

        @Override
        public String getAliasName() {
            return "Alias name";
        }

        @Override
        public String getDescription() {
            return "Description";
        }

        @Override
        public String getMRID() {
            return "0.1.2.3.5.6.7.8.9.1.2.3.4.5.6.7.8";
        }

        @Override
        public String getName() {
            return "Name";
        }
    };

    @Test
    public void testGetEmptyDeviceTypeList() throws Exception {
        Finder<DeviceType> finder = mockFinder(Collections.<DeviceType>emptyList());
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("deviceTypes")).isEmpty();
    }

    @Test
    public void testGetNonExistingDeviceType() throws Exception {
        when(deviceConfigurationService.findDeviceType(12345)).thenReturn(Optional.empty());
        Response response = target("/devicetypes/12345").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllDeviceTypesWithoutPaging() throws Exception {
        Finder<DeviceType> finder = mockFinder(Arrays.asList(mockDeviceType("device type 1", 66), mockDeviceType("device type 2", 66), mockDeviceType("device type 3", 66), mockDeviceType("device type 4", 66)));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(4);
        assertThat((List) map.get("deviceTypes")).hasSize(4);
    }

    @Test
    public void testCreateDeviceTypeNonExistingProtocol() throws Exception {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.name = "newName";
        deviceTypeInfo.deviceProtocolPluggableClassName = "theProtocol";
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);

        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = Optional.empty();
        when(protocolPluggableService.findDeviceProtocolPluggableClassByName("theProtocol")).thenReturn(deviceProtocolPluggableClass);
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(nlsMessageFormat);
        Response response = target("/devicetypes/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateDeviceType() throws Exception {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.name = "newName";
        deviceTypeInfo.deviceProtocolPluggableClassName = "theProtocol";
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);

        DeviceProtocolPluggableClass protocol = mock(DeviceProtocolPluggableClass.class);
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = Optional.of(protocol);
        when(protocolPluggableService.findDeviceProtocolPluggableClassByName("theProtocol")).thenReturn(deviceProtocolPluggableClass);
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(nlsMessageFormat);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceConfigurationService.newDeviceType("newName", protocol)).thenReturn(deviceType);

        Response response = target("/devicetypes/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test(expected = BadRequestException.class)
    public void testCreateDeviceTypeEmptyProtocolName() throws Exception {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.name = "newName";
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);

        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(nlsMessageFormat);

        ClientResponse response = target("/devicetypes/").request().post(json, ClientResponse.class);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateDeviceTypeInvalidProtocolName() throws Exception {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.name = "newName";
        deviceTypeInfo.deviceProtocolPluggableClassName = "x";
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);

        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(nlsMessageFormat);
        when(protocolPluggableService.findDeviceProtocolPluggableClassByName(anyString())).thenReturn(Optional.empty());

        ClientResponse response = target("/devicetypes/").request().post(json, ClientResponse.class);
    }

    @Test
    public void testGetAllDeviceTypesWithFullPage() throws Exception {
        Finder<DeviceType> finder = mockFinder(Arrays.asList(mockDeviceType("device type 1", 66), mockDeviceType("device type 2", 66), mockDeviceType("device type 3", 66), mockDeviceType("device type 4", 66)));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").queryParam("start", 0).queryParam("limit", 4).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(4);
        assertThat((List) map.get("deviceTypes")).hasSize(4);
    }

    @Test
    public void testGetEmptyDeviceTypeListPaged() throws Exception {
        Finder<DeviceType> finder = mockFinder(Collections.<DeviceType>emptyList());
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").queryParam("start", 100).queryParam("limit", 20).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(100);
        assertThat((List) map.get("deviceTypes")).isEmpty();
        ArgumentCaptor<QueryParameters> queryParametersArgumentCaptor = ArgumentCaptor.forClass(QueryParameters.class);
        verify(finder).from(queryParametersArgumentCaptor.capture());
        assertThat(queryParametersArgumentCaptor.getValue().getStart()).isEqualTo(100);
        assertThat(queryParametersArgumentCaptor.getValue().getLimit()).isEqualTo(20);
    }

    @Test
    public void testGetDeviceTypeByName() throws Exception {
        String webRTUKP = "WebRTUKP";
        DeviceType deviceType = mockDeviceType(webRTUKP, 66);
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));

        Map<String, Object> map = target("/devicetypes/66").request().get(Map.class);
        assertThat(map.get("name")).isEqualTo(webRTUKP);
    }

    @Test
    public void testGetLogBookTypesForDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("any", 84);
        when(deviceConfigurationService.findDeviceType(84)).thenReturn(Optional.of(deviceType));

        List logBooksList = new ArrayList();
        logBooksList.add(mockLogBookType(1, "first", "0.0.0.0.1"));
        logBooksList.add(mockLogBookType(2, "second", "0.0.0.0.2"));
        when(deviceType.getLogBookTypes()).thenReturn(logBooksList);

        Map<String, Object> map = target("/devicetypes/84/logbooktypes").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        List<Map> logBookTypeInfos = (List) map.get("logbookTypes");
        assertThat(logBookTypeInfos.size()).isEqualTo(2);
        assertThat(logBookTypeInfos.get(0).get("id")).isEqualTo(1);
        assertThat(logBookTypeInfos.get(1).get("obisCode")).isEqualTo("0.0.0.0.2");
    }

    private LogBookType mockLogBookType(long id, String name, String obisCode) {
        LogBookType logBookType = mock(LogBookType.class);
        when(logBookType.getId()).thenReturn(id);
        when(logBookType.getName()).thenReturn(name);
        ObisCode obisCodeObj = mock(ObisCode.class);
        when(obisCodeObj.toString()).thenReturn(obisCode);
        when(logBookType.getObisCode()).thenReturn(obisCodeObj);
        return logBookType;
    }

    private DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        return deviceType;
    }

    private DeviceConfiguration mockDeviceConfiguration(String name, long id) {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name);
        when(deviceConfiguration.getId()).thenReturn(id);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        RegisterType registerType = mock(RegisterType.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerType.getId()).thenReturn(101L);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.asList(registerSpec));
        return deviceConfiguration;
    }

    @Test
    public void testDeviceTypeInfoJavaScriptMappings() throws Exception {
        int NUMBER_OF_CONFIGS = 4;
        int NUMBER_OF_LOADPROFILES = 6;
        int NUMBER_OF_REGISTERS = 8;
        int NUMBER_OF_LOGBOOKS = 10;

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("unique name");
        when(deviceType.getId()).thenReturn(13L);
        List configsList = mock(List.class);
        when(configsList.size()).thenReturn(NUMBER_OF_CONFIGS);
        List loadProfileList = mock(List.class);
        when(loadProfileList.size()).thenReturn(NUMBER_OF_LOADPROFILES);
        List registerList = Arrays.asList(new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo());
        List logBooksList = mock(List.class);
        when(logBooksList.size()).thenReturn(NUMBER_OF_LOGBOOKS);

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol name");
        when(deviceType.getConfigurations()).thenReturn(configsList);
        when(deviceType.getLoadProfileTypes()).thenReturn(loadProfileList);
        when(deviceType.canActAsGateway()).thenReturn(true);
        when(deviceType.isDirectlyAddressable()).thenReturn(true);
        when(deviceType.getLogBookTypes()).thenReturn(logBooksList);
        when(deviceType.getRegisterTypes()).thenReturn(registerList);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);

        Finder<DeviceType> finder = mockFinder(Arrays.asList(deviceType));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed").isEqualTo(1);
        assertThat((List) map.get("deviceTypes")).hasSize(1).describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonDeviceType = (Map) ((List) map.get("deviceTypes")).get(0);
        assertThat(jsonDeviceType.get("id")).isEqualTo(13).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("logBookCount")).isEqualTo(NUMBER_OF_LOGBOOKS).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("registerCount")).isEqualTo(NUMBER_OF_REGISTERS).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("deviceConfigurationCount")).isEqualTo(NUMBER_OF_CONFIGS).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("loadProfileCount")).isEqualTo(NUMBER_OF_LOADPROFILES).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("name")).isEqualTo("unique name").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("canBeGateway")).isEqualTo(true).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("canBeDirectlyAddressed")).isEqualTo(true).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("deviceProtocolPluggableClass")).isEqualTo("device protocol name").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.containsKey("registerTypes")).describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void testDeviceConfigurationInfoJavaScriptMappings() throws Exception {

        DeviceType deviceType = mock(DeviceType.class);
        List registerList = mock(List.class);
        when(registerList.size()).thenReturn(2);
        List logBookList = mock(List.class);
        when(logBookList.size()).thenReturn(3);
        List loadProfileList = mock(List.class);
        when(loadProfileList.size()).thenReturn(4);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(113L);
        when(deviceConfiguration.getName()).thenReturn("defcon");
        when(deviceConfiguration.isActive()).thenReturn(true);
        when(deviceConfiguration.getDescription()).thenReturn("describe me");
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(registerList);
        when(deviceConfiguration.getLoadProfileSpecs()).thenReturn(loadProfileList);
        when(deviceConfiguration.getLogBookSpecs()).thenReturn(logBookList);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceFunction()).thenReturn(DeviceFunction.METER);
        when(deviceConfiguration.canActAsGateway()).thenReturn(true);
        when(deviceConfiguration.getGetwayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceConfiguration.canBeDirectlyAddressable()).thenReturn(true);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol name");
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);

        Finder<DeviceConfiguration> finder = mockFinder(deviceType.getConfigurations());
        when(deviceConfigurationService.findDeviceConfigurationsUsingDeviceType(any(DeviceType.class))).thenReturn(finder);
        when(deviceConfigurationService.findDeviceType(6)).thenReturn(Optional.of(deviceType));

        Map<String, Object> map = target("/devicetypes/6/deviceconfigurations").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed").isEqualTo(1);
        assertThat((List) map.get("deviceConfigurations")).hasSize(1).describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonDeviceConfiguration = (Map) ((List) map.get("deviceConfigurations")).get(0);
        assertThat(jsonDeviceConfiguration).hasSize(12);
        assertThat(jsonDeviceConfiguration.get("id")).isEqualTo(113).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("name")).isEqualTo("defcon").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("active")).isEqualTo(true).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("description")).isEqualTo("describe me").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("deviceProtocolPluggableClass")).isEqualTo("device protocol name").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("deviceFunction")).isEqualTo("Meter").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("registerCount")).isEqualTo(2).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("logBookCount")).isEqualTo(3).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("loadProfileCount")).isEqualTo(4).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("canBeGateway")).isEqualTo(true).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("gatewayType")).isEqualTo("HAN").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("isDirectlyAddressable")).isEqualTo(true).describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void testRegisterConfigurationInfoJavaScriptMappings() throws Exception {

        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(113L);
        RegisterType registerType = mock(RegisterType.class);
        ReadingType readingType = mockReadingType();
        when(registerType.getReadingType()).thenReturn(readingType);
        Phenomenon phenomenon = mock(Phenomenon.class);
        when(registerType.getPhenomenon()).thenReturn(phenomenon);
        TextualRegisterSpec registerSpec = mock(TextualRegisterSpec.class);
        when(registerSpec.isTextual()).thenReturn(true);
        when(registerSpec.getId()).thenReturn(1L);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        ObisCode obisCode = mockObisCode();
        when(registerSpec.getObisCode()).thenReturn(obisCode);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.<RegisterSpec>asList(registerSpec));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));

        when(deviceConfigurationService.findDeviceType(6)).thenReturn(Optional.of(deviceType));

        Map<String, Object> jsonRegisterConfiguration = target("/devicetypes/6/deviceconfigurations/113/registerconfigurations/1").request().get(Map.class);
        assertThat(jsonRegisterConfiguration).hasSize(15);
        assertThat(jsonRegisterConfiguration.get("id")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("name")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("readingType")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("registerType")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("obisCode")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("overruledObisCode")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("obisCodeDescription")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("unitOfMeasure")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("numberOfDigits")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("numberOfFractionDigits")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("multiplier")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("overflowValue")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("timeOfUse")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("multiplierMode")).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonRegisterConfiguration.get("asText")).describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void testRegisterTypesInfoJavaScriptMappings() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        RegisterType registerType = mock(RegisterType.class);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType));

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceFunction()).thenReturn(DeviceFunction.METER);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol name");
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        ReadingType readingType = READING_TYPE;
        when(registerType.getReadingType()).thenReturn(readingType);

        List<RegisterSpec> registerSpecs = mock(List.class);
        when(registerSpecs.size()).thenReturn(1);
        when(deviceConfigurationService.findActiveRegisterSpecsByDeviceTypeAndRegisterType(deviceType, registerType)).thenReturn(registerSpecs);
        when(deviceConfigurationService.findDeviceType(6)).thenReturn(Optional.of(deviceType));

        Map<String, Object> map = target("/devicetypes/6/registertypes").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed").isEqualTo(1);
        assertThat((List) map.get("registerTypes")).hasSize(1).describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonDeviceConfiguration = (Map) ((List) map.get("registerTypes")).get(0);
        assertThat(jsonDeviceConfiguration).containsKey("isLinkedByActiveRegisterConfig").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration).containsKey("isLinkedByInactiveRegisterConfig").describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void testUnlinkSingleNonExistingRegisterTypeFromDeviceType() throws Exception {
        // Backend has RM 101, UI wants to remove 102
        long RM_ID_1 = 101L;

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(Optional.of(deviceType));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.<DeviceProtocolPluggableClass>mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);
        when(masterDataService.findRegisterType(RM_ID_1)).thenReturn(Optional.of(registerType101));

        Response response = target("/devicetypes/31/registertypes/102").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testUpdateRegistersWithoutChanges() throws Exception {
        // Backend has RM 101 and 102, UI sets for 101 and 102: no changes
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        RegisterTypeInfo registerTypeInfo1 = new RegisterTypeInfo();
        registerTypeInfo1.id = RM_ID_1;
        registerTypeInfo1.name = "mapping 1";
        registerTypeInfo1.obisCode = new ObisCode(1, 11, 2, 12, 3, 13);
        RegisterTypeInfo registerTypeInfo2 = new RegisterTypeInfo();
        registerTypeInfo2.id = RM_ID_2;
        registerTypeInfo2.name = "mapping 2";
        registerTypeInfo2.obisCode = new ObisCode(11, 111, 12, 112, 13, 113);

        DeviceType deviceType = mockDeviceType("updater", 31L);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101, registerType102));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(Optional.of(deviceType));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.<DeviceProtocolPluggableClass>mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerTypes = Arrays.asList(registerTypeInfo1, registerTypeInfo2);
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // Nothing deleted, nothing added
        verify(deviceType, never()).removeRegisterType(any(RegisterType.class));
        verify(deviceType, never()).addRegisterType(any(RegisterType.class));
    }

    @Test
    public void testUpdateRegistersAddOneRegister() throws Exception {
        // Backend has RM 101, UI sets for 101 and 102: 102 should be added
        RegisterTypeInfo registerTypeInfo1 = new RegisterTypeInfo();
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        registerTypeInfo1.id = RM_ID_1;
        registerTypeInfo1.name = "mapping 1";
        registerTypeInfo1.obisCode = new ObisCode(1, 11, 2, 12, 3, 13);
        RegisterTypeInfo registerTypeInfo2 = new RegisterTypeInfo();
        registerTypeInfo2.id = RM_ID_2;
        registerTypeInfo2.name = "mapping 2";
        registerTypeInfo2.obisCode = new ObisCode(11, 111, 12, 112, 13, 113);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterType measurementType101 = mock(RegisterType.class);
        when(measurementType101.getId()).thenReturn(RM_ID_1);
        RegisterType measurementType102 = mock(RegisterType.class);
        when(measurementType102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(measurementType101));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(Optional.of(deviceType));
        when(masterDataService.findRegisterType(RM_ID_1)).thenReturn(Optional.of(measurementType101));
        when(masterDataService.findRegisterType(RM_ID_2)).thenReturn(Optional.of(measurementType102));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.<DeviceProtocolPluggableClass>mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerTypes = Arrays.asList(registerTypeInfo1, registerTypeInfo2);
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(deviceType, never()).removeRegisterType(any(RegisterType.class));
        verify(deviceType).addRegisterType(measurementType102);
    }

    @Test
    public void testUpdateDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration101 = mock(DeviceConfiguration.class);
        when(deviceConfiguration101.getId()).thenReturn(101L);
        when(deviceConfiguration101.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration deviceConfiguration102 = mock(DeviceConfiguration.class);
        when(deviceConfiguration102.getId()).thenReturn(102L);
        when(deviceConfiguration102.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration101, deviceConfiguration102));
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));

        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        deviceConfigurationInfo.name = "new name";
        deviceConfigurationInfo.canBeGateway = true;
        deviceConfigurationInfo.isDirectlyAddressable = true;
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/101").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration101).setName("new name");
        verify(deviceConfiguration101).setCanBeDirectlyAddressed(true);
        verify(deviceConfiguration101).setCanActAsGateway(true);
    }

    @Test
    public void testUpdateDeviceConfigurationWithoutGatewayAndDirectAddrressJsonFields() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration101 = mock(DeviceConfiguration.class);
        when(deviceConfiguration101.getId()).thenReturn(101L);
        when(deviceConfiguration101.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration deviceConfiguration102 = mock(DeviceConfiguration.class);
        when(deviceConfiguration102.getId()).thenReturn(102L);
        when(deviceConfiguration102.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration101, deviceConfiguration102));
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));

        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        deviceConfigurationInfo.name = "new name";
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/101").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration101).setName("new name");
        verify(deviceConfiguration101, never()).setCanBeDirectlyAddressed(anyBoolean());
        verify(deviceConfiguration101, never()).setCanActAsGateway(anyBoolean());
    }

    @Test
    public void testCreateDeviceConfigurationWithoutGatewayAndDirectAddressJsonFields() throws Exception {
        DeviceType deviceType = mockDeviceType("creater", 31L);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = mock(DeviceType.DeviceConfigurationBuilder.class, Answers.RETURNS_SELF);
        when(deviceType.newConfiguration("new name")).thenReturn(deviceConfigurationBuilder);
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));
        DeviceConfiguration returnValue = mock(DeviceConfiguration.class);
        when(deviceConfigurationBuilder.add()).thenReturn(returnValue);
        when(returnValue.getId()).thenReturn(1L);
        when(returnValue.getDeviceType()).thenReturn(deviceType);
        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        deviceConfigurationInfo.name = "new name";
        deviceConfigurationInfo.description = "desc";
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfigurationBuilder, never()).canActAsGateway(anyBoolean());
        verify(deviceConfigurationBuilder, never()).isDirectlyAddressable(anyBoolean());
        verify(deviceConfigurationBuilder).description("desc");
    }

    @Test
    public void testCreateDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("creater", 31L);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = mock(DeviceType.DeviceConfigurationBuilder.class, Answers.RETURNS_SELF);
        when(deviceType.newConfiguration("new name")).thenReturn(deviceConfigurationBuilder);
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));
        DeviceConfiguration returnValue = mock(DeviceConfiguration.class);
        when(deviceConfigurationBuilder.add()).thenReturn(returnValue);
        when(returnValue.getId()).thenReturn(1L);
        when(returnValue.getDeviceType()).thenReturn(deviceType);
        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        deviceConfigurationInfo.name = "new name";
        deviceConfigurationInfo.description = "desc";
        deviceConfigurationInfo.canBeGateway = true;
        deviceConfigurationInfo.isDirectlyAddressable = false;
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfigurationBuilder).canActAsGateway(true);
        verify(deviceConfigurationBuilder).isDirectlyAddressable(false);
        verify(deviceConfigurationBuilder).description("desc");
    }

    @Test
    public void testDeleteDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration101 = mock(DeviceConfiguration.class);
        when(deviceConfiguration101.getId()).thenReturn(101L);
        when(deviceConfiguration101.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration deviceConfiguration102 = mock(DeviceConfiguration.class);
        when(deviceConfiguration102.getId()).thenReturn(102L);
        when(deviceConfiguration102.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration101, deviceConfiguration102));
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));

        Response response = target("/devicetypes/31/deviceconfigurations/101").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceType).removeConfiguration(deviceConfiguration101);
    }

    @Test
    public void testDeleteNonExistingDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        when(deviceType.getConfigurations()).thenReturn(new ArrayList<DeviceConfiguration>());
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));

        Response response = target("/devicetypes/31/deviceconfigurations/101").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testActivateDeviceConfigurationIgnoresOtherProperties() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration101 = mock(DeviceConfiguration.class);
        when(deviceConfiguration101.getId()).thenReturn(101L);
        when(deviceConfiguration101.isActive()).thenReturn(false);
        when(deviceConfiguration101.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration deviceConfiguration102 = mock(DeviceConfiguration.class);
        when(deviceConfiguration102.getId()).thenReturn(102L);
        when(deviceConfiguration102.isActive()).thenReturn(false);
        when(deviceConfiguration102.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration101, deviceConfiguration102));
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));

        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        deviceConfigurationInfo.name = "new name";
        deviceConfigurationInfo.active = true;
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/101/status").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration101, never()).setName(anyString());
        verify(deviceConfiguration101, times(1)).activate();
    }

    @Test
    public void testUpdateRegistersAddNoneExistingRegisterType() throws Exception {
        // Backend has RM 101, UI sets for 101 and 102: 102 should be added but does not exist
        RegisterTypeInfo registerTypeInfo1 = new RegisterTypeInfo();
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        registerTypeInfo1.id = RM_ID_1;
        registerTypeInfo1.name = "mapping 1";
        registerTypeInfo1.obisCode = new ObisCode(1, 11, 2, 12, 3, 13);
        RegisterTypeInfo registerTypeInfo2 = new RegisterTypeInfo();
        registerTypeInfo2.id = RM_ID_2;
        registerTypeInfo2.name = "mapping 2";
        registerTypeInfo2.obisCode = new ObisCode(11, 111, 12, 112, 13, 113);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(Optional.of(deviceType));
        when(masterDataService.findRegisterType(RM_ID_1)).thenReturn(Optional.of(registerType101));
        when(masterDataService.findRegisterType(RM_ID_2)).thenReturn(Optional.empty());
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.<DeviceProtocolPluggableClass>mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerTypes = Arrays.asList(registerTypeInfo1, registerTypeInfo2);
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testUpdateRegistersDeleteOneMapping() throws Exception {
        // Backend has RM 101 and 102, UI sets for 101: delete 102
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        RegisterTypeInfo registerTypeInfo1 = new RegisterTypeInfo();
        registerTypeInfo1.id = RM_ID_1;
        registerTypeInfo1.name = "mapping 1";
        registerTypeInfo1.obisCode = new ObisCode(1, 11, 2, 12, 3, 13);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101, registerType102));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.<DeviceProtocolPluggableClass>mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(Optional.of(deviceType));
        when(masterDataService.findRegisterType(RM_ID_1)).thenReturn(Optional.of(registerType101));
        when(masterDataService.findRegisterType(RM_ID_2)).thenReturn(Optional.of(registerType102));

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerTypes = Arrays.asList(registerTypeInfo1);
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // Nothing deleted, nothing updated
        verify(deviceType).removeRegisterType(registerType102);
        verify(deviceType, never()).addRegisterType(any(RegisterType.class));
    }

    @Test
    public void testRegistersForDeviceTypeWithoutFilterAreSorted() throws Exception {
        // Backend has RM 101 and 102 for device type 31
        long deviceType_id = 31;
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterType registerType101 = mock(RegisterType.class);
        ReadingType readingType = READING_TYPE;
        when(registerType101.getId()).thenReturn(RM_ID_1);
        when(registerType101.getReadingType()).thenReturn(readingType);
        when(registerType101.getName()).thenReturn("zzz");
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(registerType102.getReadingType()).thenReturn(readingType);
        when(registerType102.getName()).thenReturn("aaa");
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101, registerType102));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));

        Map response = target("/devicetypes/31/registertypes").request().get(Map.class);
        assertThat(response).hasSize(2);
        List<Map> registerTypes = (List) response.get("registerTypes");
        assertThat(registerTypes).hasSize(2);
        assertThat(registerTypes.get(0).get("name")).isEqualTo("aaa");
        assertThat(registerTypes.get(1).get("name")).isEqualTo("zzz");
    }

    @Test
    public void testGetAllAvailableRegistersForDeviceType_Filtered() throws Exception {
        // Backend has RM 101 for device type 31, while 101, 102 and 103 exist in the server.
        long deviceType_id = 31;
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        long RM_ID_3 = 103L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        ReadingType readingType = READING_TYPE;
        when(registerType101.getReadingType()).thenReturn(readingType);
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(registerType102.getReadingType()).thenReturn(readingType);
        RegisterType registerType103 = mock(RegisterType.class);
        when(registerType103.getId()).thenReturn(RM_ID_3);
        when(registerType103.getReadingType()).thenReturn(readingType);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        Finder<RegisterType> registerTypeFinder = mockFinder(Arrays.asList(registerType101, registerType102, registerType103));
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);

        Map response = target("/devicetypes/31/registertypes").queryParam("filter", ExtjsFilter.filter().property("available", "true").create()).request().get(Map.class);
        assertThat(response).hasSize(2);
        List registerTypes = (List) response.get("registerTypes");
        assertThat(registerTypes).hasSize(2);
    }

    @Test
    public void testGetAllAvailableRegistersForDeviceType_FilteredByConfig() throws Exception {
        long deviceType_id = 31;
        long deviceConfiguration_id = 41;
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        long RM_ID_3 = 103L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("config", (int) deviceConfiguration_id);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        ReadingType readingType = READING_TYPE;
        when(registerType101.getReadingType()).thenReturn(readingType);
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(registerType102.getReadingType()).thenReturn(readingType);
        RegisterType registerType103 = mock(RegisterType.class);
        when(registerType103.getId()).thenReturn(RM_ID_3);
        when(registerType103.getReadingType()).thenReturn(readingType);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101, registerType102, registerType103));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));

        Finder<RegisterType> registerTypeFinder = mockFinder(Arrays.asList(registerType101, registerType102, registerType103));
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);

        Map response = target("/devicetypes/31/registertypes").queryParam("filter", ExtjsFilter.filter().property("available", "true").property("deviceconfigurationid", 41l).create()).request().get(Map.class);
        assertThat(response).hasSize(2);
        List registerTypes = (List) response.get("registerTypes");
        assertThat(registerTypes).hasSize(2);
    }


    @Test
    public void testGetDeviceCommunicationById() throws Exception {
        long deviceType_id = 41;
        long deviceConfiguration_id = 14;
        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        DeviceConfiguration mock1 = mock(DeviceConfiguration.class);
        when(mock1.getId()).thenReturn(deviceConfiguration_id + 1);
        DeviceConfiguration mock2 = mock(DeviceConfiguration.class);
        when(mock2.getId()).thenReturn(deviceConfiguration_id + 2);
        DeviceConfiguration mock3 = mock(DeviceConfiguration.class);
        when(mock3.getDeviceType()).thenReturn(deviceType);
        when(mock3.getId()).thenReturn(deviceConfiguration_id);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(mock1, mock2, mock3));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        Response response = target("/devicetypes/41/deviceconfigurations/14").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetDeviceCommunicationByIdButNoSuchConfigOnTheDevice() throws Exception {
        long deviceType_id = 41;
        long deviceConfiguration_id = 14;
        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        DeviceConfiguration mock1 = mock(DeviceConfiguration.class);
        when(mock1.getId()).thenReturn(deviceConfiguration_id + 1);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(mock1));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        Response response = target("/devicetypes/41/deviceconfigurations/14").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetDeviceCommunicationByIdWithNonExistingDeviceType() throws Exception {
        long deviceType_id = 41;
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.empty());
        Response response = target("/devicetypes/41/deviceconfigurations/14").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllRegisterConfig() throws Exception {
        long deviceType_id = 41;
        long deviceConfig_id = 51;
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(deviceConfig_id);
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Collections.<RegisterSpec>emptyList());

        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetAllRegisterConfigById() throws Exception {
        long deviceType_id = 41;
        long deviceConfig_id = 51;
        long registerConfig_id = 61;
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(deviceConfig_id);
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        NumericalRegisterSpec registerSpec = mock(NumericalRegisterSpec.class);
        when(registerSpec.getId()).thenReturn(registerConfig_id);
        when(registerSpec.isTextual()).thenReturn(false);
        ReadingType readingType = mockReadingType();
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getReadingType()).thenReturn(readingType);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        Phenomenon phenomenon = mock(Phenomenon.class);
        when(registerType.getPhenomenon()).thenReturn(phenomenon);
        ObisCode obisCode = mockObisCode();
        when(registerSpec.getObisCode()).thenReturn(obisCode);

        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.<RegisterSpec>asList(registerSpec));

        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/61").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testCreateRegisterConfigWithoutLinkedChannelSpec() throws Exception {
        long deviceType_id = 41;
        long deviceConfig_id = 51;
        long registerType_id = 133;
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(deviceConfig_id);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        RegisterType registerType = mock(RegisterType.class);
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        when(masterDataService.findRegisterType(registerType_id)).thenReturn(Optional.of(registerType));
        ReadingType readingType = mockReadingType();
        when(registerType.getReadingType()).thenReturn(readingType);
        NumericalRegisterSpec registerConfig = mock(NumericalRegisterSpec.class);
        when(registerConfig.getRegisterType()).thenReturn(registerType);
        ObisCode obisCode = mockObisCode();
        when(registerConfig.getObisCode()).thenReturn(obisCode);
        NumericalRegisterSpec.Builder registerSpecBuilder = mock(NumericalRegisterSpec.Builder.class, Answers.RETURNS_SELF);
        when(registerSpecBuilder.add()).thenReturn(registerConfig);
        when(deviceConfiguration.createNumericalRegisterSpec(Matchers.<RegisterType>any())).thenReturn(registerSpecBuilder);
        Phenomenon phenomenon = mock(Phenomenon.class);
        when(registerType.getPhenomenon()).thenReturn(phenomenon);
        RegisterConfigInfo registerConfigInfo = new RegisterConfigInfo();
        registerConfigInfo.registerType = registerType_id;
        registerConfigInfo.multiplier = BigDecimal.TEN;
        registerConfigInfo.numberOfFractionDigits = 6;
        registerConfigInfo.numberOfDigits = 4;
        registerConfigInfo.overflow = BigDecimal.TEN;
        registerConfigInfo.overruledObisCode = null;

        Entity<RegisterConfigInfo> json = Entity.json(registerConfigInfo);
        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        ArgumentCaptor<RegisterType> registerTypeArgumentCaptor = ArgumentCaptor.forClass(RegisterType.class);
        verify(registerSpecBuilder).setMultiplier(BigDecimal.TEN);
        verify(registerSpecBuilder).setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        verify(registerSpecBuilder).setNumberOfDigits(4);
        verify(registerSpecBuilder).setNumberOfFractionDigits(6);
        verify(registerSpecBuilder).setOverflowValue(BigDecimal.TEN);
        verify(deviceConfiguration).createNumericalRegisterSpec(registerTypeArgumentCaptor.capture());
        assertThat(registerTypeArgumentCaptor.getValue()).isEqualTo(registerType);
    }

    @Test
    public void testUpdateRegisterConfigWithoutLinkedChannelSpec() throws Exception {
        long deviceType_id = 41;
        long deviceConfig_id = 51;
        long registerSpec_id = 61;
        long registerType_id = 133;
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(deviceConfig_id);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        RegisterType registerType = mock(RegisterType.class);
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        when(masterDataService.findRegisterType(registerType_id)).thenReturn(Optional.of(registerType));
        ReadingType readingType = mockReadingType();
        when(registerType.getReadingType()).thenReturn(readingType);
        NumericalRegisterSpec registerConfig = mock(NumericalRegisterSpec.class);
        when(registerConfig.getRegisterType()).thenReturn(registerType);
        when(registerConfig.getId()).thenReturn(registerSpec_id);
        ObisCode obisCode = mockObisCode();
        when(registerConfig.getObisCode()).thenReturn(obisCode);
        NumericalRegisterSpec.Builder registerSpecBuilder = mock(NumericalRegisterSpec.Builder.class, Answers.RETURNS_SELF);
        when(registerSpecBuilder.add()).thenReturn(registerConfig);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.<RegisterSpec>asList(registerConfig));
        Phenomenon phenomenon = mock(Phenomenon.class);
        when(registerType.getPhenomenon()).thenReturn(phenomenon);
        RegisterConfigInfo registerConfigInfo = new RegisterConfigInfo();
        registerConfigInfo.registerType = registerType_id;
        registerConfigInfo.multiplier = BigDecimal.TEN;
        registerConfigInfo.numberOfFractionDigits = 6;
        registerConfigInfo.numberOfDigits = 4;
        registerConfigInfo.overflow = BigDecimal.valueOf(123);
        registerConfigInfo.overruledObisCode = obisCode;

        Entity<RegisterConfigInfo> json = Entity.json(registerConfigInfo);
        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/61").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<ObisCode> obisCodeArgumentCaptor = ArgumentCaptor.forClass(ObisCode.class);
        verify(registerConfig).setMultiplier(BigDecimal.TEN);
        verify(registerConfig).setRegisterType(registerType);
        verify(registerConfig).setOverruledObisCode(obisCodeArgumentCaptor.capture());
        assertThat(obisCodeArgumentCaptor.getValue().toString()).isEqualTo(obisCode.toString());
        verify(registerConfig).setOverflowValue(BigDecimal.valueOf(123));
        verify(registerConfig).setNumberOfDigits(4);
        verify(registerConfig).setNumberOfFractionDigits(6);
        verify(registerConfig).save();
    }

    @Test
    @Ignore // TODO Re-enable once Environement is removed from SimplePropertyType
    public void testGetAllConnectionMethodJavaScriptMappings() throws Exception {
        long deviceType_id = 41L;
        long deviceConfig_id = 51L;
        long connectionMethodId = 61L;
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(deviceConfig_id);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(connectionTypePluggableClass.getName()).thenReturn("connection type PC");
        ConnectionType connectionType = mock(ConnectionType.class);
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        when(partialConnectionTask.getId()).thenReturn(connectionMethodId);
        when(partialConnectionTask.getName()).thenReturn("connection method");
        PropertySpec<String> propertySpec1 = mock(PropertySpec.class);
        when(propertySpec1.getName()).thenReturn("macAddress");
        when(propertySpec1.getValueFactory()).thenReturn(new StringFactory());
        TypedProperties typedProperties = new TypedProperties();
        typedProperties.setProperty("macAddress", "aa:bb:cc:dd:ee:ff");
        when(partialConnectionTask.getTypedProperties()).thenReturn(typedProperties);
        when(connectionType.getPropertySpecs()).thenReturn(Arrays.<PropertySpec>asList(propertySpec1));
        when(partialConnectionTask.getPluggableClass()).thenReturn(connectionTypePluggableClass);
        when(partialConnectionTask.getPluggableClass()).thenReturn(connectionTypePluggableClass);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        Map<String, Object> response = target("/devicetypes/41/deviceconfigurations/51/connectionmethods").request().get(Map.class);
        assertThat(response).hasSize(2);
        assertThat(response.get("total")).isEqualTo(1);
        List<Map<String, Object>> connectionMethods = (List<Map<String, Object>>) response.get("connectionMethods");
        assertThat(connectionMethods).hasSize(1);
        Map<String, Object> connectionMethod = connectionMethods.get(0);
        assertThat(connectionMethod).hasSize(13)
                .containsKey("id")
                .containsKey("name")
                .containsKey("direction")
                .containsKey("connectionType")
                .containsKey("comWindowStart")
                .containsKey("comWindowEnd")
                .containsKey("isDefault")
                .containsKey("allowSimultaneousConnections")
                .containsKey("rescheduleRetryDelay")
                .containsKey("connectionStrategy")
                .containsKey("properties")
                .containsKey("temporalExpression");
        List<Map<String, Object>> propertyInfos = (List<Map<String, Object>>) connectionMethod.get("properties");
        assertThat(propertyInfos).isNotNull().hasSize(1);
        Map<String, Object> macAddressProperty = propertyInfos.get(0);
        assertThat(macAddressProperty).hasSize(4)
                .containsKey("key")
                .containsKey("propertyValueInfo")
                .containsKey("propertyTypeInfo")
                .containsKey("required");
        Map<String, Object> propertyValueInfo = (Map<String, Object>) macAddressProperty.get("propertyValueInfo");
        assertThat(propertyValueInfo).hasSize(3)
                .containsKey("inheritedValue")
                .containsKey("defaultValue")
                .containsKey("value");
        Map<String, Object> propertyTypeInfo = (Map<String, Object>) macAddressProperty.get("propertyTypeInfo");
        assertThat(propertyTypeInfo).hasSize(4)
                .containsKey("simplePropertyType")
                .containsKey("propertyValidationRule")
                .containsKey("predefinedPropertyValuesInfo")
                .containsKey("referenceUri");
    }

    @Test
    @Ignore // TODO Re-enable once Environement is removed from SimplePropertyType
    public void testUpdateConnectionMethodNormalProperties() throws Exception {
        long deviceType_id = 41L;
        long deviceConfig_id = 51L;
        long connectionMethodId = 71L;
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(deviceConfig_id);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(connectionMethodId);
        when(deviceConfigurationService.getPartialConnectionTask(connectionMethodId)).thenReturn(Optional.<PartialConnectionTask>of(partialConnectionTask));
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(protocolPluggableService.findConnectionTypePluggableClassByName("ConnType")).thenReturn(Optional.of(connectionTypePluggableClass));
        when(partialConnectionTask.getPluggableClass()).thenReturn(connectionTypePluggableClass); // it will not be set in the PUT!
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTypePluggableClass.getConnectionType()).thenReturn(connectionType);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));

        ScheduledConnectionMethodInfo connectionMethodInfo = new ScheduledConnectionMethodInfo();
        connectionMethodInfo.name = "connection method";
        connectionMethodInfo.id = connectionMethodId;
        connectionMethodInfo.comWindowStart = 3600;
        connectionMethodInfo.comWindowEnd = 7200;
        connectionMethodInfo.isDefault = true;
        connectionMethodInfo.allowSimultaneousConnections = true;
        connectionMethodInfo.connectionTypePluggableClass = "ConnType";
        Entity<ScheduledConnectionMethodInfo> json = Entity.json(connectionMethodInfo);
        Response response = target("/devicetypes/41/deviceconfigurations/51/connectionmethods/71").request().put(json);
    }

    @Test
    public void testDeleteRegisterConfig() throws Exception {
        long deviceType_id = 41L;
        long deviceConfig_id = 51L;
        long registerSpec_id = 61L;
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(deviceConfig_id);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        when(registerSpec.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(registerSpec.getId()).thenReturn(registerSpec_id);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.asList(registerSpec));
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));

        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/61").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration).deleteRegisterSpec(registerSpec);
    }

    @Test
    public void testCreateRegisterConfigWithLinkToNonExistingRegisterType() throws Exception {
        long deviceType_id = 41L;
        long deviceConfig_id = 51L;
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(deviceConfig_id);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        when(registerSpec.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.asList(registerSpec));
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(nlsMessageFormat);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        RegisterConfigInfo registerConfigInfo = new RegisterConfigInfo();
        when(masterDataService.findRegisterType(12345)).thenReturn(Optional.empty());
        registerConfigInfo.registerType = 12345L;
        Entity<RegisterConfigInfo> json = Entity.json(registerConfigInfo);
        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testConstraintViolationResultsInProperJson() throws Exception {
        // Backend has RM 101 and 102, UI sets for 101: delete 102
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        RegisterTypeInfo registerTypeInfo1 = new RegisterTypeInfo();
        registerTypeInfo1.id = RM_ID_1;
        registerTypeInfo1.name = "mapping 1";
        registerTypeInfo1.obisCode = new ObisCode(1, 11, 2, 12, 3, 13);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101, registerType102));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(Optional.of(deviceType));
        when(masterDataService.findRegisterType(RM_ID_1)).thenReturn(Optional.of(registerType101));
        when(masterDataService.findRegisterType(RM_ID_2)).thenReturn(Optional.of(registerType102));

        Thesaurus thesaurus = mock(Thesaurus.class);
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(nlsMessageFormat);
        MessageSeed messageSeed = mock(MessageSeed.class);
        doThrow(new SomeLocalizedException(thesaurus, messageSeed)).when(deviceType).save();

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerTypes = Arrays.asList(registerTypeInfo1);
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        ByteArrayInputStream entity = (ByteArrayInputStream) response.getEntity();
        byte[] bytes = new byte[entity.available()];
        entity.read(bytes, 0, entity.available());
        String answer = new String(bytes);
        assertThat(answer).contains("\"message\"").contains("\"errors\"");
    }

    @Test
    public void testGetAvailableConnectionMethodsForDevice() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("random", 32);
        Device device = mock(Device.class);

        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceService.findByUniqueMrid("Z666")).thenReturn(device);
        ConnectionTask<?, ?> connectionTask1 = mockConnectionTask(101L);
        ConnectionTask<?, ?> connectionTask2 = mockConnectionTask(102L);
        ConnectionTask<?, ?> connectionTask3 = mockConnectionTask(103L);
        when(device.getConnectionTasks()).thenReturn(Arrays.<ConnectionTask<?, ?>>asList(connectionTask3, connectionTask1, connectionTask2));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        PartialConnectionTask partialConnectionTask1 = mockPartialConnectionTask(101L);
        PartialConnectionTask partialConnectionTask2 = mockPartialConnectionTask(102L);
        PartialConnectionTask partialConnectionTask3 = mockPartialConnectionTask(103L);
        PartialConnectionTask partialConnectionTask4 = mockPartialConnectionTask(104L);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask1, partialConnectionTask2, partialConnectionTask4, partialConnectionTask3));
        Map<String, Object> response = target("/devicetypes/31/deviceconfigurations/32/connectionmethods/").queryParam("available", "true").queryParam("mrId", "Z666").request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        assertThat(data.get(0).get("id")).isEqualTo(104);
        assertThat(response).isNotNull();

    }

    private ConnectionTask<?, ?> mockConnectionTask(long partialConnectionTaskId) {
        ConnectionTask<?, ?> mock = mock(ConnectionTask.class);
        PartialInboundConnectionTask partialInboundConnectionTask = mockPartialConnectionTask(partialConnectionTaskId);
        Mockito.<PartialConnectionTask>when(mock.getPartialConnectionTask()).thenReturn(partialInboundConnectionTask);
        return mock;
    }

    private PartialInboundConnectionTask mockPartialConnectionTask(long partialConnectionTaskId) {
        PartialInboundConnectionTask partialInboundConnectionTask = mock(PartialInboundConnectionTask.class);
        when(partialInboundConnectionTask.getId()).thenReturn(partialConnectionTaskId);
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggableClass.getName()).thenReturn("someClass");
        when(partialInboundConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(partialInboundConnectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        return partialInboundConnectionTask;
    }


    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.defaultSortColumn(anyString())).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }

    private ObisCode mockObisCode() {
        ObisCode obisCode = mock(ObisCode.class);
        when(obisCode.getDescription()).thenReturn("desc");
        when(obisCode.toString()).thenReturn("1.1.1.1.1.1");
        return obisCode;
    }

    private ReadingType mockReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("mrid");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        return readingType;
    }

    class SomeLocalizedException extends LocalizedException {

        protected SomeLocalizedException(Thesaurus thesaurus, MessageSeed messageSeed) {
            super(thesaurus, messageSeed);
        }
    }

}
