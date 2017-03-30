/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.StreetDetail;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.cbo.TownDetail;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.geo.Position;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceLocationImplTest extends EqualsContractTest {

    private static final long ID = 35L;
    public static final long INSTANCE_A_ID = 64L;

    private ServiceLocationImpl serviceLocation, instanceA;

    @Mock
    private UsagePoint usagePoint1, usagePoint2;
    @Mock
    private DataModel dataModel;
    @Mock
    private DataMapper<ServiceLocation> serviceLocationFactory;
    @Mock
    private DataMapper<UsagePoint> usagePointFactory;
    @Mock
    private EventService eventService;

    @Before
    public void setUp() {
        when(dataModel.mapper(ServiceLocation.class)).thenReturn(serviceLocationFactory);
        when(dataModel.mapper(UsagePoint.class)).thenReturn(usagePointFactory);
        serviceLocation = new ServiceLocationImpl(dataModel, eventService);
    }

    @After
    public void tearDown() {
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new ServiceLocationImpl(dataModel, eventService);
            field("id").ofType(Long.TYPE).in(instanceA).set(INSTANCE_A_ID);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        ServiceLocationImpl other = new ServiceLocationImpl(dataModel, eventService);
        field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID);
        return other;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ServiceLocationImpl other = new ServiceLocationImpl(dataModel, eventService);
        field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID + 1);
        return Collections.singletonList(other);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
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

        assertThat(serviceLocation.getElectronicAddress()).isEqualToComparingFieldByField(electronicAddress);
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

        assertThat(serviceLocation.getMainAddress().getStreetDetail()).isEqualToComparingFieldByField(streetAddress.getStreetDetail());
        assertThat(serviceLocation.getMainAddress().getTownDetail()).isEqualToComparingFieldByField(streetAddress.getTownDetail());
    }

    @Test
    public void testGetPhone1() {
        TelephoneNumber telephoneNumber = new TelephoneNumber("+32", "9", "555 55 55");
        serviceLocation.setPhone1(telephoneNumber);

        assertThat(serviceLocation.getPhone1()).isEqualToComparingFieldByField(telephoneNumber);
    }

    @Test
    public void testGetPhone2() {
        TelephoneNumber telephoneNumber = new TelephoneNumber("+32", "9", "555 55 55");
        serviceLocation.setPhone2(telephoneNumber);

        assertThat(serviceLocation.getPhone2()).isEqualToComparingFieldByField(telephoneNumber);
    }

    @Test
    public void testGetSecondaryAddress() {
        StreetAddress streetAddress = new StreetAddress(new StreetDetail("street", "nr"), new TownDetail("cd", "Paris", "France"));
        serviceLocation.setSecondaryAddress(streetAddress);

        assertThat(serviceLocation.getSecondaryAddress().getStreetDetail()).isEqualToComparingFieldByField(streetAddress.getStreetDetail());
        assertThat(serviceLocation.getSecondaryAddress().getTownDetail()).isEqualToComparingFieldByField(streetAddress.getTownDetail());
    }

    @Test
    public void testGetStatus() {
        Status status = Status.builder().value("value").reason("reason").remark("remark").build();
        serviceLocation.setStatus(status);

        assertThat(serviceLocation.getStatus()).isEqualToComparingFieldByField(status);
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
        serviceLocation.update();

        verify(serviceLocationFactory).persist(serviceLocation);
    }

    @Test
    public void testSaveUpdate() {
        simulateSavedServiceLocation();
        serviceLocation.update();

        verify(serviceLocationFactory).update(serviceLocation);
    }

    @Test
    public void testDelete() {
        simulateSavedServiceLocation();
        serviceLocation.delete();

        verify(serviceLocationFactory).remove(serviceLocation);
    }

    @Test
    public void testGetUsagePoints() {
        when(usagePointFactory.find("serviceLocation", serviceLocation, "obsoleteTime", null)).thenReturn(Arrays.asList(usagePoint1, usagePoint2));

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
