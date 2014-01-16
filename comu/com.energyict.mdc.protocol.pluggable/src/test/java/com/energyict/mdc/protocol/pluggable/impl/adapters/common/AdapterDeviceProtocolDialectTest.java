package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol;
import org.junit.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


/**
 * Tests the {@link AdapterDeviceProtocolDialect} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 15:59
 */
public class AdapterDeviceProtocolDialectTest {

    private static final String REQUIRED_PROPERTY_NAME = "RequiredProperty";
    private static final String OPTIONAL_PROPERTY_NAME = "OptionalProperty";
    private static final String FIRST_ADDITIONAL_PROPERTY_NAME = "FirstAdditionalProperty";
    private static final String SECOND_ADDITIONAL_PROPERTY_NAME = "SecondAdditionalProperty";

    private static InMemoryPersistence inMemoryPersistence;
    private static ProtocolPluggableService protocolPluggableService;

    @BeforeClass
    public static void initializeDatabase() {
        inMemoryPersistence = InMemoryPersistence.initializeDatabase();
        protocolPluggableService = inMemoryPersistence.getProtocolPluggableService();
    }

    @AfterClass
    public static void cleanupDatabase () throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public AdapterDeviceProtocolDialectTest () {
    }

    private List<PropertySpec> getOptionalPropertiesFromSet(List<PropertySpec> propertySpecs) {
        List<PropertySpec> requiredProperties = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            if(!propertySpec.isRequired()){
                requiredProperties.add(propertySpec);
            }
        }
        return requiredProperties;
    }

    private List<PropertySpec> getRequiredPropertiesFromSet(List<PropertySpec> propertySpecs) {
        List<PropertySpec> requiredProperties = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            if(propertySpec.isRequired()){
                requiredProperties.add(propertySpec);
            }
        }
        return requiredProperties;
    }

    @Test
    public void testDialectName () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(dialect.getDeviceProtocolDialectName()).isEqualTo("MockMeterProto1479312711");
    }

    @Test
    public void getRequiredKeysTest () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(getRequiredPropertiesFromSet(dialect.getPropertySpecs())).containsOnly(getRequiredPropertySpec());

    }

    @Test
    public void getOptionalKeysTest () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(getOptionalPropertiesFromSet(dialect.getPropertySpecs())).containsOnly(getOptionalPropertySpec());
    }

    @Test
    public void getPropertySpecTest () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(dialect.getPropertySpec(REQUIRED_PROPERTY_NAME)).isEqualTo(getRequiredPropertySpec());
        assertThat(dialect.getPropertySpec(OPTIONAL_PROPERTY_NAME)).isEqualTo(getOptionalPropertySpec());
    }

    @Test
    public void testWithAdditionalProperties () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(dialect.getPropertySpecs()).containsOnly(this.getPropertySpecs());
    }

    @Test
    public void testWithRemovableProperties () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        List<PropertySpec> removableProperties = Arrays.asList(getFirstRemovableProperty(), getSecondRemovableProperty());
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(protocolPluggableService, mockDeviceProtocol, removableProperties);

        assertThat(dialect.getPropertySpecs()).containsOnly(this.getOptionalPropertySpec());
    }

    @Test
    public void testWithOnlyAdditionalProperties () {
        MeterProtocol mockDeviceProtocol = mock(MeterProtocol.class);
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(dialect.getPropertySpecs()).isEmpty();
    }

    @Test
    public void testWithOnlyRemovableProperties () {
        MeterProtocol mockDeviceProtocol = mock(MeterProtocol.class);
        List<PropertySpec> removableProperties = Arrays.asList(getFirstRemovableProperty(), getSecondRemovableProperty());
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(protocolPluggableService, mockDeviceProtocol, removableProperties);

        assertThat(dialect.getPropertySpecs()).isEmpty();
    }

    private PropertySpec<String>[] getPropertySpecs () {
        PropertySpec<String>[] allPropertySpecs = new PropertySpec[2];
        allPropertySpecs[0] = this.getRequiredPropertySpec();
        allPropertySpecs[1] = this.getOptionalPropertySpec();
        return allPropertySpecs;
    }

    private PropertySpec<String> getRequiredPropertySpec () {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(REQUIRED_PROPERTY_NAME);
    }

    private PropertySpec<String> getOptionalPropertySpec () {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(OPTIONAL_PROPERTY_NAME);
    }

    private PropertySpec getFirstRemovableProperty () {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(REQUIRED_PROPERTY_NAME);
    }

    private PropertySpec getSecondRemovableProperty () {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(FIRST_ADDITIONAL_PROPERTY_NAME);
    }

}