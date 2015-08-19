package com.energyict.mdc.device.data.impl.security;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.NestedRelationTransactionException;
import com.energyict.mdc.device.data.exceptions.SecurityPropertyException;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationSearchFilter;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames;
import com.google.common.collect.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link SecurityPropertyServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-15 (09:010)
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityPropertyServiceImplTest {
    private static final long SECURITY_PROPERTY_SET1_ID = 97L;
    private static final long SECURITY_PROPERTY_SET2_ID = 103L;
    private static final long ETERNITY = 1_000_000_000_000_000_000L;
    private static final String USERNAME_SECURITY_PROPERTY_NAME = "username";
    private static final String PASSWORD_SECURITY_PROPERTY_NAME = "password";
    private static final String SOME_KEY_SECURITY_PROPERTY_NAME = "someKey";

    @Mock
    private PluggableService pluggableService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private SecurityPropertySet securityPropertySet1;
    @Mock
    private SecurityPropertySet securityPropertySet2;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private Device device;
    @Mock
    private RelationType securityPropertyRelationType;
    @Mock
    private Clock clock;
    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private DeviceProtocolMessageService deviceProtocolMessageService;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    @Mock
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    @Mock
    private ConnectionTypeService connectionTypeService;
    @Mock
    private LicensedProtocolService licensedProtocolService;
    @Mock
    private DeviceCacheMarshallingService deviceCacheMarshallingService;
    @Mock
    private NlsService nlsService;

    @Before
    public void initializeMocks () {
        when(clock.instant()).thenReturn(Instant.now());
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet1, this.securityPropertySet2));
        ComTaskEnablement cte1 = mock(ComTaskEnablement.class);
        when(cte1.getSecurityPropertySet()).thenReturn(this.securityPropertySet1);
        ComTaskEnablement cte2 = mock(ComTaskEnablement.class);
        when(cte2.getSecurityPropertySet()).thenReturn(this.securityPropertySet2);
        when(this.deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(cte1, cte2));
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        when(this.pluggableService
                .newPluggableClass(PluggableClassType.DeviceProtocol, "SecurityPropertyServiceImplTest", TestProtocolWithOnlySecurityProperties.class.getName()))
                .thenReturn(this.deviceProtocolPluggableClass);
        when(this.protocolPluggableService.findSecurityPropertyRelationType(this.deviceProtocolPluggableClass)).thenReturn(this.securityPropertyRelationType);
        when(this.protocolPluggableService.isLicensedProtocolClassName(anyString())).thenReturn(true);

        when(this.securityPropertySet1.getId()).thenReturn(SECURITY_PROPERTY_SET1_ID);
        when(this.securityPropertySet1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);
        when(this.securityPropertySet1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
        PropertySpec userName = mock(PropertySpec.class);
        when(userName.getName()).thenReturn(USERNAME_SECURITY_PROPERTY_NAME);
        when(userName.isRequired()).thenReturn(true);
        when(userName.getValueFactory()).thenReturn(new StringFactory());
        PropertySpec password = mock(PropertySpec.class);
        when(password.getName()).thenReturn(PASSWORD_SECURITY_PROPERTY_NAME);
        when(password.isRequired()).thenReturn(true);
        when(password.getValueFactory()).thenReturn(new StringFactory());
        when(this.securityPropertySet1.getPropertySpecs()).thenReturn(new HashSet<>(Arrays.asList(userName, password)));
        when(this.securityPropertySet2.getId()).thenReturn(SECURITY_PROPERTY_SET2_ID);
        when(this.securityPropertySet2.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);
        when(this.securityPropertySet2.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
        PropertySpec someKey = mock(PropertySpec.class);
        when(someKey.getName()).thenReturn(SOME_KEY_SECURITY_PROPERTY_NAME);
        when(someKey.getValueFactory()).thenReturn(new StringFactory());
        when(someKey.isRequired()).thenReturn(true);
        when(this.securityPropertySet2.getPropertySpecs()).thenReturn(new HashSet<>(Arrays.asList(someKey)));
    }

    @Test
    public void hasSecurityPropertiesForUserThatIsNotAllowedToView () {
        Relation relation = mock(Relation.class);
        Instant relationFrom = Instant.ofEpochSecond(97L);
        Instant relationTo = relationFrom.plusSeconds(10);
        when(relation.getPeriod()).thenReturn(Range.closedOpen(relationFrom, relationTo));
        when(relation.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("user");
        when(relation.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("password");
        when(this.securityPropertyRelationType.findByFilter(any(RelationSearchFilter.class))).thenReturn(Arrays.asList(relation));
        when(this.securityPropertySet1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(false);

        // Business method
        boolean hasSecurityProperties = this.testService().hasSecurityProperties(this.device, relationFrom.plusSeconds(1), this.securityPropertySet1);

        // Asserts
        assertThat(hasSecurityProperties).isTrue();
    }

    @Test
    public void hasSecurityPropertiesForUserThatIsAllowedToView () {
        Relation relation = mock(Relation.class);
        Instant relationFrom = Instant.ofEpochSecond(97L);
        Instant relationTo = relationFrom.plusSeconds(10);
        when(relation.getPeriod()).thenReturn(Range.closedOpen(relationFrom, relationTo));
        when(relation.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("user");
        when(relation.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("password");
        when(this.securityPropertyRelationType.findByFilter(any(RelationSearchFilter.class))).thenReturn(Arrays.asList(relation));
        when(this.securityPropertySet1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);

        // Business method
        boolean hasSecurityProperties = this.testService().hasSecurityProperties(this.device, relationFrom.plusSeconds(1), this.securityPropertySet1);

        // Asserts
        assertThat(hasSecurityProperties).isTrue();
    }

    @Test
    public void securityPropertiesAreValidForUserThatIsNotAllowedToView () {
        Relation relation = mock(Relation.class);
        Instant relationFrom = Instant.now().minusSeconds(1L);
        Instant relationTo = relationFrom.plusSeconds(86400L);  // Add another day
        when(relation.getPeriod()).thenReturn(Range.closedOpen(relationFrom, relationTo));
        when(relation.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("user");
        when(relation.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("password");
        when(relation.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(this.securityPropertyRelationType.findByFilter(any(RelationSearchFilter.class))).thenReturn(Arrays.asList(relation));
        when(this.securityPropertySet1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(false);

        // Business method
        boolean propertiesAreValid = this.testService().securityPropertiesAreValid(this.device, this.securityPropertySet1);

        // Asserts
        assertThat(propertiesAreValid).isTrue();
    }

    @Test
    public void securityPropertiesAreValidForUserThatIsAllowedToView () {
        Relation relation = mock(Relation.class);
        Instant relationFrom = Instant.now().minusSeconds(1L);
        Instant relationTo = relationFrom.plusSeconds(86400L);  // Add another day
        when(relation.getPeriod()).thenReturn(Range.closedOpen(relationFrom, relationTo));
        when(relation.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("user");
        when(relation.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("password");
        when(relation.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(this.securityPropertyRelationType.findByFilter(any(RelationSearchFilter.class))).thenReturn(Arrays.asList(relation));
        when(this.securityPropertySet1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);

        // Business method
        boolean propertiesAreValid = this.testService().securityPropertiesAreValid(this.device, this.securityPropertySet1);

        // Asserts
        assertThat(propertiesAreValid).isTrue();
    }

    @Test
    public void hasSecurityPropertiesForRelationsInThePast() {
        Instant relation1From = Instant.ofEpochSecond(97L);
        Instant relation1To = relation1From.plusSeconds(10);
        Relation relation1 = mock(Relation.class);
        when(relation1.getPeriod()).thenReturn(Range.closedOpen(relation1From, relation1To));
        when(relation1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("old user");
        when(relation1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("old password");
        Instant relation2From = relation1To;
        Instant relation2To = relation2From.plusSeconds(100);
        Relation relation2 = mock(Relation.class);
        when(relation2.getPeriod()).thenReturn(Range.closedOpen(relation2From, relation2To));
        when(this.securityPropertyRelationType.findByFilter(any(RelationSearchFilter.class))).thenReturn(Arrays.asList(relation1, relation2));
        when(this.securityPropertySet1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);

        // Business method
        boolean hasSecurityProperties = this.testService().hasSecurityProperties(this.device, relation2To.plusSeconds(1), this.securityPropertySet1);

        // Asserts
        assertThat(hasSecurityProperties).isFalse();
    }

    @Test
    public void getSecurityPropertiesForUserThatIsNotAllowedToView () {
        Relation relation = mock(Relation.class);
        when(this.securityPropertyRelationType.findByFilter(any(RelationSearchFilter.class))).thenReturn(Arrays.asList(relation));
        when(this.securityPropertySet1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(false);

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, Instant.now(), this.securityPropertySet1);

        // Asserts
        verify(this.securityPropertySet1).currentUserIsAllowedToViewDeviceProperties();
        assertThat(securityProperties).isEmpty();
    }

    @Test
    public void getSecurityPropertiesWithoutRelationType () {
        when(this.protocolPluggableService.findSecurityPropertyRelationType(this.deviceProtocolPluggableClass)).thenReturn(null);

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, Instant.now(), this.securityPropertySet1);

        // Asserts
        verify(this.protocolPluggableService).findSecurityPropertyRelationType(this.deviceProtocolPluggableClass);
        assertThat(securityProperties).isEmpty();
    }

    @Test
    public void getSecurityPropertiesReturnsOnlyActiveProperties () throws SQLException {
        Instant oldRelationFrom = Instant.ofEpochSecond(97L);
        Instant oldRelationTo = oldRelationFrom.plusSeconds(10);
        Relation oldRelation = mock(Relation.class);
        when(oldRelation.getPeriod()).thenReturn(Range.closedOpen(oldRelationFrom, oldRelationTo));
        when(oldRelation.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("old user");
        when(oldRelation.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("old password");
        Instant currentRelationFrom = oldRelationTo;
        Instant currentRelationTo = currentRelationFrom.plusSeconds(100);
        Relation currentRelation = mock(Relation.class);
        when(currentRelation.getPeriod()).thenReturn(Range.closedOpen(currentRelationFrom, currentRelationTo));
        String expectedUser = "current user";
        String expectedPassword = "current password";
        when(currentRelation.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn(expectedUser);
        when(currentRelation.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn(expectedPassword);
        when(currentRelation.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(this.securityPropertyRelationType.findByFilter(any(RelationSearchFilter.class))).thenReturn(Arrays.asList(oldRelation, currentRelation));
        when(this.securityPropertySet1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);
        when(this.clock.instant()).thenReturn(currentRelationFrom.plusSeconds(10));

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, this.clock.instant(), this.securityPropertySet1);

        // Asserts
        verify(this.protocolPluggableService).findSecurityPropertyRelationType(this.deviceProtocolPluggableClass);
        Map<String, Object> propertyNames =
                securityProperties
                        .stream()
                        .collect(Collectors.toMap(
                            SecurityProperty::getName,
                            SecurityProperty::getValue));
        assertThat(propertyNames.keySet()).containsOnly(USERNAME_SECURITY_PROPERTY_NAME, PASSWORD_SECURITY_PROPERTY_NAME);
        assertThat(propertyNames.get(USERNAME_SECURITY_PROPERTY_NAME)).isEqualTo(expectedUser);
        assertThat(propertyNames.get(PASSWORD_SECURITY_PROPERTY_NAME)).isEqualTo(expectedPassword);
    }

    @Test
    public void getSecurityPropertiesWithOnlyPropertiesInThePast () throws SQLException {
        Instant oldRelationFrom = Instant.ofEpochSecond(97L);
        Instant oldRelationTo = oldRelationFrom.plusSeconds(10);
        Relation oldRelation = mock(Relation.class);
        when(oldRelation.getPeriod()).thenReturn(Range.closedOpen(oldRelationFrom, oldRelationTo));
        when(oldRelation.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("old user");
        when(oldRelation.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("old password");
        Instant currentRelationFrom = oldRelationTo;
        Instant currentRelationTo = currentRelationFrom.plusSeconds(100);
        Relation currentRelation = mock(Relation.class);
        when(currentRelation.getPeriod()).thenReturn(Range.closedOpen(currentRelationFrom, currentRelationTo));
        String expectedUser = "current user";
        String expectedPassword = "current password";
        when(currentRelation.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn(expectedUser);
        when(currentRelation.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn(expectedPassword);
        when(this.securityPropertyRelationType.findByFilter(any(RelationSearchFilter.class))).thenReturn(Arrays.asList(oldRelation, currentRelation));
        when(this.securityPropertySet1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);
        when(this.clock.instant()).thenReturn(currentRelationTo.plusSeconds(10));

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, this.clock.instant(), this.securityPropertySet1);

        // Asserts
        verify(this.protocolPluggableService).findSecurityPropertyRelationType(this.deviceProtocolPluggableClass);
        assertThat(securityProperties).isEmpty();
    }

    @Test(expected = SecurityPropertyException.class)
    public void setSecurityPropertiesForUserThatIsNotAllowedToEdit () {
        when(this.securityPropertySet1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(false);

        // Business method
        this.testService().setSecurityProperties(this.device, this.securityPropertySet1, TypedProperties.empty());

        // Asserts
        verify(this.securityPropertySet1).currentUserIsAllowedToEditDeviceProperties();
    }

    @Test
    public void setSecurityProperties () throws SQLException, BusinessException {
        Instant now = Instant.ofEpochSecond(97L);
        when(this.clock.instant()).thenReturn(now);
        when(this.securityPropertySet1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
        RelationTransaction transaction = mock(RelationTransaction.class);
        when(this.securityPropertyRelationType.newRelationTransaction()).thenReturn(transaction);
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty("One", BigDecimal.ONE);
        properties.setProperty("Two", "Due");

        // Business method
        this.testService().setSecurityProperties(this.device, this.securityPropertySet1, properties);

        // Asserts
        verify(transaction).setFrom(now);
        verify(transaction).setTo(null);
        verify(transaction).set(SecurityPropertySetRelationAttributeTypeNames.DEVICE_ATTRIBUTE_NAME, this.device);
        verify(transaction).set(SecurityPropertySetRelationAttributeTypeNames.SECURITY_PROPERTY_SET_ATTRIBUTE_NAME, this.securityPropertySet1);
        verify(transaction).set("One", BigDecimal.ONE);
        verify(transaction).set("Two", "Due");
        verify(transaction).execute();
    }

    @Test(expected = NestedRelationTransactionException.class)
    public void setSecurityPropertiesWrapsBusinessException () throws SQLException, BusinessException {
        Instant now = Instant.ofEpochSecond(97L);
        when(this.clock.instant()).thenReturn(now);
        when(this.securityPropertySet1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
        RelationTransaction transaction = mock(RelationTransaction.class);
        when(transaction.getRelationType()).thenReturn(this.securityPropertyRelationType);
        when(this.securityPropertyRelationType.newRelationTransaction()).thenReturn(transaction);
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty("One", BigDecimal.ONE);
        properties.setProperty("Two", "Due");
        doThrow(BusinessException.class).when(transaction).execute();

        // Business method
        this.testService().setSecurityProperties(this.device, this.securityPropertySet1, properties);

        // Asserts: see expected exception rule
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void setSecurityPropertiesWrapsSqlException () throws SQLException, BusinessException {
        Instant now = Instant.ofEpochSecond(97L);
        when(this.clock.instant()).thenReturn(now);
        when(this.securityPropertySet1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
        RelationTransaction transaction = mock(RelationTransaction.class);
        when(this.securityPropertyRelationType.newRelationTransaction()).thenReturn(transaction);
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty("One", BigDecimal.ONE);
        properties.setProperty("Two", "Due");
        doThrow(SQLException.class).when(transaction).execute();

        // Business method
        this.testService().setSecurityProperties(this.device, this.securityPropertySet1, properties);

        // Asserts: see expected exception rule
    }

    @Test
    public void securityPropertiesAreNotValidWhenAllAreMissing() {
        SecurityPropertyService service = this.testService();
        when(this.securityPropertyRelationType.findByFilter(any(RelationSearchFilter.class))).thenReturn(Collections.emptyList());

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreNotValidWhenOneIsMissing() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("pass");
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Collections.emptyList());

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreValidWhenUnusedOneIsMissing() {
        SecurityPropertySet unused = mock(SecurityPropertySet.class);
        when(unused.getId()).thenReturn(SECURITY_PROPERTY_SET2_ID + 1);
        PropertySpec otherKey = mock(PropertySpec.class);
        when(otherKey.getName()).thenReturn("Other");
        when(otherKey.getValueFactory()).thenReturn(new StringFactory());
        when(otherKey.isRequired()).thenReturn(true);
        when(unused.getPropertySpecs()).thenReturn(new HashSet<>(Arrays.asList(otherKey)));
        when(this.deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet1, this.securityPropertySet2));
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("pass");
        Relation relationForSecurityPropertySet2 = mock(Relation.class);
        when(relationForSecurityPropertySet2.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet2.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet2.get(SOME_KEY_SECURITY_PROPERTY_NAME)).thenReturn("something");
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Arrays.asList(relationForSecurityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isTrue();
    }

    @Test
    public void securityPropertiesAreNotValidWhenAllAreInComplete() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(false);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn(null);
        Relation relationForSecurityPropertySet2 = mock(Relation.class);
        when(relationForSecurityPropertySet2.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet2.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(false);
        when(relationForSecurityPropertySet2.get(SOME_KEY_SECURITY_PROPERTY_NAME)).thenReturn(null);
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Arrays.asList(relationForSecurityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreNotValidWhenSomeAreInComplete() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("pass");
        Relation relationForSecurityPropertySet2 = mock(Relation.class);
        when(relationForSecurityPropertySet2.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet2.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(false);
        when(relationForSecurityPropertySet2.get(SOME_KEY_SECURITY_PROPERTY_NAME)).thenReturn(null);
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Arrays.asList(relationForSecurityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreValid() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("pass");
        Relation relationForSecurityPropertySet2 = mock(Relation.class);
        when(relationForSecurityPropertySet2.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet2.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet2.get(SOME_KEY_SECURITY_PROPERTY_NAME)).thenReturn("something");
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Arrays.asList(relationForSecurityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isTrue();
    }

    @Test
    public void deleteSecurityProperties() throws SQLException, BusinessException {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("pass");
        Relation relationForSecurityPropertySet2 = mock(Relation.class);
        when(relationForSecurityPropertySet2.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet2.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet2.get(SOME_KEY_SECURITY_PROPERTY_NAME)).thenReturn("something");
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Arrays.asList(relationForSecurityPropertySet2));

        // Business method
        service.deleteSecurityPropertiesFor(this.device);

        // Asserts
        verify(this.protocolPluggableService).findSecurityPropertyRelationType(this.deviceProtocolPluggableClass);
        verify(this.securityPropertyRelationType, times(2)).findByFilter(any(RelationSearchFilter.class));
        verify(relationForSecurityPropertySet1).delete();
        verify(relationForSecurityPropertySet2).delete();
    }

    private SecurityPropertyService testService () {
        return new SecurityPropertyServiceImpl(this.clock, this.protocolPluggableService, this.nlsService);
    }

}