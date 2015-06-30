package com.energyict.mdc.favorites.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.FavoriteDeviceGroup;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.favorites.LabelCategory;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class FavoritesServiceImplTest {

    private Injector injector;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private UserService userService;

    private MeteringGroupsService meteringGroupsService;

    private DeviceConfigurationService deviceConfigurationService;

    private DeviceService deviceService;

    private FavoritesService favoritesService;

    private LabelCategory labelCategory;

    private User user, user1;

    private Device device, device1;

    private EndDeviceGroup endDeviceGroup, endDeviceGroup1;

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    public static final MessageSeed LABEL_CATEGORY = new MessageSeed() {
        @Override
        public String getModule() {
            return FavoritesService.COMPONENTNAME;
        }
        @Override
        public int getNumber() {
            return 0;
        }
        @Override
        public String getKey() {
            return "label.category.favorites";
        }
        @Override
        public String getDefaultFormat() {
            return "Favorites";
        }
        @Override
        public Level getLevel() {
            return Level.INFO;
        }
    };

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BpmService.class).toInstance(mock(BpmService.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(IssueService.class).toInstance(mock(IssueService.class, RETURNS_DEEP_STUBS));
        }
    }

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new UtilModule(),
                new ThreadSecurityModule(),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new MeteringModule(false),
                new MeteringGroupsModule(),
                new InMemoryMessagingModule(),
                new OrmModule(),
                new DataVaultModule(),
                new IssuesModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new ProtocolPluggableModule(),
                new EngineModelModule(),
                new MasterDataModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new FiniteStateMachineModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new MdcIOModule(),
                new BasicPropertiesModule(),
                new ProtocolApiModule(),
                new TaskModule(),
                new KpiModule(),
                new TasksModule(),
                new DeviceDataModule(),
                new SchedulingModule(),
                new FavoritesModule());

        try (TransactionContext ctx = getTransactionService().getContext()) {
            userService = injector.getInstance(UserService.class);
            injector.getInstance(FiniteStateMachineService.class);
            meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            injector.getInstance(MasterDataService.class);
            deviceService = injector.getInstance(DeviceService.class);
            favoritesService = injector.getInstance(FavoritesService.class);

            labelCategory = favoritesService.createLabelCategory("test.label.category");

            user = userService.createUser("user", "user descr");
            user1 = userService.createUser("user1", "user1 descr");

            endDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup(Condition.TRUE);
            endDeviceGroup.save();

            endDeviceGroup1 = meteringGroupsService.createQueryEndDeviceGroup(Where.where("MRID").isEqualTo("ZABF0000100001"));
            endDeviceGroup1.save();

            DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
            when(deviceProtocolPluggableClass.getId()).thenReturn(1L);
            DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
            when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
            deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            DeviceType deviceType = deviceConfigurationService.newDeviceType("Device type", deviceProtocolPluggableClass);
            deviceType.save();
            DeviceConfiguration configuration = deviceType.newConfiguration("Configuration").add();
            configuration.save();
            configuration.activate();

            device = deviceService.newDevice(configuration, "ZABF0000100001", "ZABF0000100001");
            device.save();

            device1 = deviceService.newDevice(configuration, "ZABF0000100002", "ZABF0000100002");
            device1.save();

            ctx.commit();
        }
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testInstall() {
        FavoritesServiceImpl favDevicesServiceImpl = (FavoritesServiceImpl)favoritesService;
        assertThat(favDevicesServiceImpl.getPrerequisiteModules()).contains("ORM", "USR", "NLS", "MTG", "DDC");

        //double installation
        favDevicesServiceImpl.install();
        //no exceptions
    }

    @Test
    public void testTranslationKeys() {
        FavoritesServiceImpl favoritesServiceImpl = (FavoritesServiceImpl) favoritesService;
        assertThat(favoritesServiceImpl.getLayer()).isEqualTo(Layer.DOMAIN);
        assertThat(favoritesServiceImpl.getComponentName()).isEqualTo("FAV");
        assertThat(favoritesServiceImpl.getKeys()).containsExactly(MessageSeeds.values());
    }

    @Test
    public void testGetLabelCategories() {
        List<LabelCategory> labelCategories = favoritesService.getLabelCategories();

        assertThat(labelCategories).hasSize(1);
        assertThat(labelCategories.get(0).getName()).isEqualTo("test.label.category");
    }

    @Test
    public void testCreateLabelCategory() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.createLabelCategory("category_name");

            Optional<LabelCategory> category = favoritesService.findLabelCategory("wrond name");
            assertThat(category.isPresent()).isFalse();

            category = favoritesService.findLabelCategory("category_name");
            assertThat(category.isPresent()).isTrue();
            assertThat(category.get().getName()).isEqualTo("category_name");
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" +  MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}", property = "name", strict = false)
    public void testCreateLabelCategoryWithNullName() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.createLabelCategory(null);
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" +  MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}", property = "name", strict = false)
    public void testCreateLabelCategoryWithEmptyName() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.createLabelCategory("");
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" +  MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}", property = "name", strict = false)
    public void testCreateLabelCategoryWithTooLongName() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.createLabelCategory("1234567890123456789012345678901234567890123456789012345678901234567890123456789021");
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" +  MessageSeeds.Constants.DUPLICATE_LABEL_CATEGORY + "}", property = "name", strict = false)
    public void testDuplicateLabelCategory() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.createLabelCategory("category_name");
            favoritesService.createLabelCategory("category_name");
        }
    }

    @Test
    public void testGetFavoriteDeviceGroups() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.findOrCreateFavoriteDeviceGroup(endDeviceGroup, user);
            favoritesService.findOrCreateFavoriteDeviceGroup(endDeviceGroup1, user);
            context.commit();
        }

        List<FavoriteDeviceGroup> favoriteDeviceGroupsUser = favoritesService.getFavoriteDeviceGroups(user);
        List<FavoriteDeviceGroup> favoriteDeviceGroupsUser1 = favoritesService.getFavoriteDeviceGroups(user1);

        assertThat(favoriteDeviceGroupsUser).hasSize(2);
        assertThat(favoriteDeviceGroupsUser.get(0).getEndDeviceGroup().getId()).isEqualTo(endDeviceGroup.getId());
        assertThat(favoriteDeviceGroupsUser.get(1).getEndDeviceGroup().getId()).isEqualTo(endDeviceGroup1.getId());
        assertThat(favoriteDeviceGroupsUser1).isEmpty();
    }

    @Test
    public void testCreateFavoriteDeviceGroup() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.findOrCreateFavoriteDeviceGroup(endDeviceGroup, user);
            context.commit();
        }

        Optional<FavoriteDeviceGroup> foundFavoriteDeviceGroup = favoritesService.findFavoriteDeviceGroup(endDeviceGroup, user);

        assertThat(foundFavoriteDeviceGroup.isPresent()).isTrue();
        assertThat(foundFavoriteDeviceGroup.get().getEndDeviceGroup().getId()).isEqualTo(endDeviceGroup.getId());
        assertThat(foundFavoriteDeviceGroup.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    public void testRemoveFavoriteDeviceGroup() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.findOrCreateFavoriteDeviceGroup(endDeviceGroup, user);
            favoritesService.findOrCreateFavoriteDeviceGroup(endDeviceGroup1, user);
            context.commit();
        }
        FavoriteDeviceGroup favoriteDeviceGroup = favoritesService.findFavoriteDeviceGroup(endDeviceGroup, user).get();
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.removeFavoriteDeviceGroup(favoriteDeviceGroup);
            context.commit();
        }

        List<FavoriteDeviceGroup> favoriteDeviceGroups = favoritesService.getFavoriteDeviceGroups(user);
        assertThat(favoriteDeviceGroups).hasSize(1);
        assertThat(favoriteDeviceGroups.get(0).getEndDeviceGroup().getId()).isEqualTo(endDeviceGroup1.getId());

        Optional<FavoriteDeviceGroup> removedFavoriteDeviceGroup = favoritesService.findFavoriteDeviceGroup(endDeviceGroup, user);
        assertThat(removedFavoriteDeviceGroup.isPresent()).isFalse();
    }

    @Test
    public void testGetDeviceLabels() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.findOrCreateDeviceLabel(device, user, labelCategory, "comment");
            favoritesService.findOrCreateDeviceLabel(device1, user, labelCategory, "comment");
            context.commit();
        }

        List<DeviceLabel> deviceLabelsOfUser = favoritesService.getDeviceLabelsOfCategory(user, labelCategory);
        List<DeviceLabel> deviceLabelsOfUser1 = favoritesService.getDeviceLabelsOfCategory(user1, labelCategory);

        assertThat(deviceLabelsOfUser).hasSize(2);
        assertThat(deviceLabelsOfUser.get(0).getDevice().getId()).isEqualTo(device.getId());
        assertThat(deviceLabelsOfUser.get(1).getDevice().getId()).isEqualTo(device1.getId());
        assertThat(deviceLabelsOfUser1).isEmpty();
    }

    @Test
    public void testCreateDeviceLabel() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.findOrCreateDeviceLabel(device, user, labelCategory, "Some comment...");
            context.commit();
        }

        Optional<DeviceLabel> foundDeviceLabel = favoritesService.findDeviceLabel(device, user, labelCategory);

        assertThat(foundDeviceLabel.isPresent()).isTrue();
        assertThat(foundDeviceLabel.get().getDevice().getId()).isEqualTo(device.getId());
        assertThat(foundDeviceLabel.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(foundDeviceLabel.get().getComment()).isEqualTo("Some comment...");
        assertThat(foundDeviceLabel.get().getCreationDate()).isNotNull();
    }

    @Test
    public void testRemoveDeviceLabel() {
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.findOrCreateDeviceLabel(device, user, labelCategory, "comment");
            favoritesService.findOrCreateDeviceLabel(device1, user, labelCategory, "comment");
            context.commit();
        }
        DeviceLabel deviceLabel = favoritesService.findDeviceLabel(device, user, labelCategory).get();
        try (TransactionContext context = getTransactionService().getContext()) {
            favoritesService.removeDeviceLabel(deviceLabel);
            context.commit();
        }

        List<DeviceLabel> deviceLabels = favoritesService.getDeviceLabelsOfCategory(user, labelCategory);
        assertThat(deviceLabels).hasSize(1);
        assertThat(deviceLabels.get(0).getDevice().getId()).isEqualTo(device1.getId());

        Optional<DeviceLabel> removedDeviceLabel = favoritesService.findDeviceLabel(device, user, labelCategory);
        assertThat(removedDeviceLabel.isPresent()).isFalse();
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

}
