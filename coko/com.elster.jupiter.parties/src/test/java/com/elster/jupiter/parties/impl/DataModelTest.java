package com.elster.jupiter.parties.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;


public class DataModelTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {       
           bind(BundleContext.class).toInstance(mock(BundleContext.class));   
           bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
        }
    }
    
    private static final boolean printSql = true;

    @BeforeClass
    public static void setUp() throws SQLException {
        injector = Guice.createInjector(
        			new MockModule(), 
        			inMemoryBootstrapModule,  
        			new PartyModule(), 
        			new UserModule(),
        			new EventsModule(),
        			new InMemoryMessagingModule(),
        			new DomainUtilModule(), 
        			new OrmModule(),
        			new UtilModule(), 
        			new ThreadSecurityModule(), 
        			new PubSubModule(), 
        			new TransactionModule(printSql));
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
        	injector.getInstance(PartyService.class);
        	ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }

    private PartyService getPartyService() {
        return injector.getInstance(PartyService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }
    
    @Test
    public void testInheritance()  {
        try (TransactionContext context = getTransactionService().getContext()) {
        	PartyServiceImpl partyService = (PartyServiceImpl) getPartyService();
         	Organization organization = partyService.newOrganization("Melrose");
        	organization.setAliasName("Melrose Place");
        	organization.setDescription("Buy and Improve");
        	organization.save();
        	com.elster.jupiter.orm.DataModel dataModel = partyService.getDataModel();
        	assertThat(dataModel.mapper(Party.class).find()).hasSize(1);
        	assertThat(dataModel.mapper(Organization.class).find()).hasSize(1);
        	assertThat(dataModel.mapper(Person.class).find()).hasSize(0);
        	long id = organization.getId();
        	assertThat(dataModel.mapper(Party.class).getOptional(id)).isPresent();
        	assertThat(dataModel.mapper(Organization.class).getOptional(id)).isPresent();
        	assertThat(dataModel.mapper(Person.class).getOptional(id)).isAbsent();
        	assertThat(dataModel.mapper(Party.class).getEager(id)).isPresent();
        	assertThat(dataModel.mapper(Organization.class).getEager(id)).isPresent();
        	assertThat(dataModel.mapper(Person.class).getEager(id)).isAbsent();
        	partyService.createRole("XXX", "YYY", "ZZZ", "AAA", "BBB");
        	PartyRole role = partyService.getPartyRoles().get(0);
        	organization.assumeRole(role,new Date());
        	Condition condition = Where.where("party.description").isEqualTo(organization.getDescription());
        	assertThat(dataModel.query(PartyInRole.class, Party.class).select(condition)).hasSize(1);
        	User user = injector.getInstance(UserService.class).findUser("admin").get();
        	Date start = new Date();
        	PartyRepresentation representation = organization.appointDelegate(user, start);
        	representation = dataModel.mapper(PartyRepresentation.class).getOptional(user.getName(),organization.getId(),start.getTime()).get();
        	dataModel.touch(representation);
        	context.commit();
        }
    }
    
    @Test
    public void testWriteBack()  {
        try (TransactionContext context = getTransactionService().getContext()) {
        	PartyServiceImpl partyService = (PartyServiceImpl) getPartyService();
         	PersonImpl person = (PersonImpl) partyService.newPerson("Frank","Hyldmar");
         	assertThat(person.getCreateTime()).isNull();
         	assertThat(person.getModTime()).isNull();
         	assertThat(person.getUserName()).isNull();
         	person.save();
         	Date now = new Date();
         	assertThat(person.getCreateTime()).isBeforeOrEqualsTo(now);
         	assertThat(person.getModTime()).isBeforeOrEqualsTo(now);
         	assertThat(person.getUserName()).isNotNull();
        	assertThat(person.getVersion()).isEqualTo(0);
         	person.setAliasName("xxxx");
         	person.save();
         	assertThat(person.getCreateTime()).isBeforeOrEqualsTo(now);
         	assertThat(person.getModTime()).isAfterOrEqualsTo(now);
         	assertThat(person.getVersion()).isEqualTo(1);
        }
    }
    
    @Test(expected=IllegalStateException.class)
    public void testQueryRestriction() {
		PartyServiceImpl partyService = (PartyServiceImpl) getPartyService();
		DataModel dataModel = partyService.getDataModel();
		dataModel.query(PartyInRole.class, Person.class);
    }
    
    @Test
    public void testQueryNoRestriction() {
		PartyServiceImpl partyService = (PartyServiceImpl) getPartyService();
		DataModel dataModel = partyService.getDataModel();
		dataModel.query(PartyInRole.class, Party.class);
		dataModel.query(PartyInRole.class, PartyImpl.class);
    }
}
