package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.StreetDetail;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.cbo.TownDetail;
import com.elster.jupiter.parties.Organization;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateOrganizationTransactionTest {

    private static final int ID = 1656;
    private static final Date DATE = new Date(3516161L);
    private static final TelephoneNumber PHONE1 = new TelephoneNumber("32", "9", "555 55 55 55");
    private static final TelephoneNumber PHONE2 = new TelephoneNumber("32", "9", "666 66 66 66");
    private static final String MRID = "MRID";

    @Captor
    private ArgumentCaptor<TelephoneNumber> telephoneCaptor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private Organization organization;

    @Before
    public void setUp() {
        Bus.setServiceLocator(serviceLocator);

        when(serviceLocator.getPartyService().newOrganization(MRID)).thenReturn(organization);

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testPerformTrivial() throws Exception {
        CreateOrganizationTransaction transaction = new CreateOrganizationTransaction(organizationInfo());

        Organization result = transaction.perform();

        assertThat(result).isEqualTo(organization);

        verify(organization).setPhone1(telephoneCaptor.capture());

        assertThat(telephoneCaptor.getValue()).isEqualsToByComparingFields(PHONE1);
    }

    private OrganizationInfo organizationInfo() {
        OrganizationInfo info = new OrganizationInfo();
        info.mRID = MRID;
        info.name = "Name";
        info.aliasName = "alias";
        info.description = "description";
        info.electronicAddress = new ElectronicAddress("mail@elster.com");
        info.phone1 = PHONE1;
        info.phone2 = PHONE2;
        info.version = 18;
        info.postalAddress = new PostalAddress("code", "poBox", new StreetDetail("street", "nr"), new TownDetail("tcd", "town", "section", "province", "country"));
        info.streetAddress = new StreetAddress(new StreetDetail("street", "nr"), new TownDetail("tcd", "town", "section", "province", "country"), new Status("active", "noreason", "noremark", DATE));
        return info;
    }
}
