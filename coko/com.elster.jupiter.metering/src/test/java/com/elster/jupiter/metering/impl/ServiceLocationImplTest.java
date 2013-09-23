package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.StreetDetail;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.cbo.TownDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.metering.plumbing.ServiceLocator;
import com.elster.jupiter.util.geo.Position;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceLocationImplTest {

    private static final long ID = 35L;

    private ServiceLocationImpl serviceLocation;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private UsagePoint usagePoint1, usagePoint2;

    @Before
    public void setUp() {
        serviceLocation = new ServiceLocationImpl();

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testGetId() {
        simulateSavedServiceLocation();

        assertThat(serviceLocation.getId()).isEqualTo(ID);
    }

    @Test
    public void testGetDescription() {
        String description = "description";
        serviceLocation.setDescription(description);

        assertThat(serviceLocation.getDescription()).isEqualTo(description);
    }

    @Test
    public void testGetAliasName() {
        String aliasName = "aliasName";
        serviceLocation.setAliasName(aliasName);

        assertThat(serviceLocation.getAliasName()).isEqualTo(aliasName);
    }

    @Test
    public void testGetMRID() {
        String mRID = "mRID";
        serviceLocation.setMRID(mRID);

        assertThat(serviceLocation.getMRID()).isEqualTo(mRID);
    }

    @Test
    public void testGetName() {
        String name = "name";
        serviceLocation.setName(name);

        assertThat(serviceLocation.getName()).isEqualTo(name);
    }

    @Test
    public void testGetDirection() {
        String direction = "direction";
        serviceLocation.setDirection(direction);

        assertThat(serviceLocation.getDirection()).isEqualTo(direction);
    }

    @Test
    public void testGetElectronicAddress() {
        ElectronicAddress electronicAddress = new ElectronicAddress("email@mail.mars");
        serviceLocation.setElectronicAddress(electronicAddress);

        assertThat(serviceLocation.getElectronicAddress()).isEqualsToByComparingFields(electronicAddress);
    }

    @Test
    public void testSetGeoInfoReference() {
        String geoInfoReference = "geoInfoReference";
        serviceLocation.setGeoInfoReference(geoInfoReference);

        assertThat(serviceLocation.getGeoInfoReference()).isEqualTo(geoInfoReference);
    }

    @Test
    public void testGetMainAddress() {
        StreetAddress streetAddress = new StreetAddress(new StreetDetail("street", "nr"), new TownDetail("cd", "Paris", "France"));
        serviceLocation.setMainAddress(streetAddress);

        assertThat(serviceLocation.getMainAddress().getStreetDetail()).isEqualsToByComparingFields(streetAddress.getStreetDetail());
        assertThat(serviceLocation.getMainAddress().getTownDetail()).isEqualsToByComparingFields(streetAddress.getTownDetail());
    }

    @Test
    public void testGetPhone1() {
        TelephoneNumber telephoneNumber = new TelephoneNumber("+32", "9", "555 55 55");
        serviceLocation.setPhone1(telephoneNumber);

        assertThat(serviceLocation.getPhone1()).isEqualsToByComparingFields(telephoneNumber);
    }

    @Test
    public void testGetPhone2() {
        TelephoneNumber telephoneNumber = new TelephoneNumber("+32", "9", "555 55 55");
        serviceLocation.setPhone2(telephoneNumber);

        assertThat(serviceLocation.getPhone2()).isEqualsToByComparingFields(telephoneNumber);
    }

    @Test
    public void testGetSecondaryAddress() {
        StreetAddress streetAddress = new StreetAddress(new StreetDetail("street", "nr"), new TownDetail("cd", "Paris", "France"));
        serviceLocation.setSecondaryAddress(streetAddress);

        assertThat(serviceLocation.getSecondaryAddress().getStreetDetail()).isEqualsToByComparingFields(streetAddress.getStreetDetail());
        assertThat(serviceLocation.getSecondaryAddress().getTownDetail()).isEqualsToByComparingFields(streetAddress.getTownDetail());
    }

    @Test
    public void testGetStatus() {
        Status status = Status.builder().value("value").reason("reason").remark("remark").build();
        serviceLocation.setStatus(status);

        assertThat(serviceLocation.getStatus()).isEqualsToByComparingFields(status);
    }

    @Test
    public void testGetType() {
        String type = "type";
        serviceLocation.setType(type);

        assertThat(serviceLocation.getType()).isEqualTo(type);
    }

    @Test
    public void testGetAccessMethod() {
        String access = "accessMethod";
        serviceLocation.setAccessMethod(access);

        assertThat(serviceLocation.getAccessMethod()).isEqualTo(access);
    }

    @Test
    public void testIsNeedsInspection() {
        serviceLocation.setNeedsInspection(true);

        assertThat(serviceLocation.isNeedsInspection()).isTrue();
    }

    @Test
    public void testGetSiteAccessProblem() {
        String accessProblem = "accessProblem";
        serviceLocation.setSiteAccessProblem(accessProblem);

        assertThat(serviceLocation.getSiteAccessProblem()).isEqualTo(accessProblem);
    }

    @Test
    public void testSaveNew() {
        serviceLocation.save();

        verify(serviceLocator.getOrmClient().getServiceLocationFactory()).persist(serviceLocation);
    }

    @Test
    public void testSaveUpdate() {
        simulateSavedServiceLocation();
        serviceLocation.save();

        verify(serviceLocator.getOrmClient().getServiceLocationFactory()).update(serviceLocation);
    }

    @Test
    public void testDelete() {
        simulateSavedServiceLocation();
        serviceLocation.delete();

        verify(serviceLocator.getOrmClient().getServiceLocationFactory()).remove(serviceLocation);
    }

    @Test
    public void testGetUsagePoints() {
        when(serviceLocator.getOrmClient().getUsagePointFactory().find("serviceLocation", serviceLocation)).thenReturn(Arrays.asList(usagePoint1, usagePoint2));

        assertThat(serviceLocation.getUsagePoints()).hasSize(2)
                .contains(usagePoint1)
                .contains(usagePoint2);
    }

    @Test
    public void testGetPosition() {
        serviceLocation.setGeoInfoReference("GG,40.714167,-74.006389");

        assertThat(serviceLocation.getPosition()).isEqualTo(new Position(new BigDecimal("40.714167"), new BigDecimal("-74.006389")));
    }

    @Test
    public void testGetPositionNull() {
        serviceLocation.setGeoInfoReference(null);

        assertThat(serviceLocation.getPosition()).isNull();
    }

    @Test
    public void testGetPositionNullIfEmptyString() {
        serviceLocation.setGeoInfoReference("");

        assertThat(serviceLocation.getPosition()).isNull();
    }

    @Test
    public void testGetPositionNullIfMisformatted() {
        serviceLocation.setGeoInfoReference("GG,40:50");

        assertThat(serviceLocation.getPosition()).isNull();
    }


    private void simulateSavedServiceLocation() {
        field("id").ofType(Long.TYPE).in(serviceLocation).set(ID);
    }

}
