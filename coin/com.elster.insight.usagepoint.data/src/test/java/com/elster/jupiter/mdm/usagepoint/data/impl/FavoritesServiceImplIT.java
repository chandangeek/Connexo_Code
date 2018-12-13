/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.StreetDetail;
import com.elster.jupiter.cbo.TownDetail;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.mdm.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePoint;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoritesService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.impl.ElectricityDetailImpl;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FavoritesServiceImplIT {
    private static UsagePointDataInMemoryBootstrapModule inMemoryBootstrapModule =
            new UsagePointDataInMemoryBootstrapModule();

    private static final Instant NOW = Instant.now();
    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");
    private static Clock clock = mock(Clock.class);

    private static ThreadPrincipalService threadPrincipalService;
    private static FavoritesService favoritesService;

    private static User user1, user2;
    private static UsagePoint usagePoint1, usagePoint2;
    private static UsagePointGroup usagePointGroup1, usagePointGroup2;

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() throws SQLException {
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZONE);
        inMemoryBootstrapModule.activate(clock);
        try (TransactionContext ctx = inMemoryBootstrapModule.getTransactionService().getContext()) {
            threadPrincipalService = inMemoryBootstrapModule.getThreadPrincipalService();
            UserService userService = inMemoryBootstrapModule.getUserService();
            MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
            MeteringGroupsService meteringGroupsService = inMemoryBootstrapModule.getMeteringGroupsService();
            favoritesService = inMemoryBootstrapModule.getFavoritesService();

            user1 = userService.createUser("user1", "user1 descr");
            user2 = userService.createUser("user2", "user2 descr");

            SearchDomain searchDomain = mock(SearchDomain.class);
            when(searchDomain.getId()).thenReturn(UsagePoint.class.getName());
            usagePointGroup1 = meteringGroupsService.createQueryUsagePointGroup()
                    .setName("QUPG1")
                    .setSearchDomain(searchDomain)
                    .setQueryProviderName("QueryProviderName")
                    .create();

            usagePointGroup2 = meteringGroupsService.createQueryUsagePointGroup()
                    .setName("QUPG2")
                    .setSearchDomain(searchDomain)
                    .setQueryProviderName("QueryProviderName")
                    .create();

            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                    .orElseThrow(() -> new NoSuchElementException("No service category " + ServiceKind.ELECTRICITY + " found."));
            ServiceLocation location = meteringService.newServiceLocation()
                    .setMainAddress(new StreetAddress(new StreetDetail("Rodionova", "23"), new TownDetail("831", "Nizhny Novgorod", "RU")))
                    .setName("Mera")
                    .create();
            usagePoint1 = serviceCategory.newUsagePoint("UP_1", Instant.EPOCH).withServiceLocation(location).create();
            usagePoint1.setServiceLocation(location);
            ElectricityDetailImpl detail = (ElectricityDetailImpl) serviceCategory.newUsagePointDetail(usagePoint1, Instant.now());
            detail.setRatedPower(Unit.WATT.amount(BigDecimal.valueOf(1000), 3));
            usagePoint1.addDetail(detail);

            usagePoint2 = serviceCategory.newUsagePoint("UP_2", Instant.EPOCH).withServiceLocation(location).create();

            ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "usagePoint", strict = false)
    public void testCreateFavoriteUsagePointWithNullUsagePoint() {
        threadPrincipalService.set(user1);
        favoritesService.markFavorite((UsagePoint)null);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "usagePointGroup", strict = false)
    public void testCreateFavoriteUsagePointGroupWithNullUsagePointGroup() {
        threadPrincipalService.set(user1);
        favoritesService.markFavorite((UsagePointGroup)null);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "user", strict = false)
    public void testCreateFavoriteUsagePointWithNullUser() {
        threadPrincipalService.set(() -> "Fake!");
        favoritesService.markFavorite(usagePoint1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "user", strict = false)
    public void testCreateFavoriteUsagePointGroupWithNullUser() {
        threadPrincipalService.set(() -> "Fake!");
        favoritesService.markFavorite(usagePointGroup1);
    }

    @Test
    @Transactional
    public void testGetFavoriteUsagePointsAndGroups() {
        List<FavoriteUsagePointGroup> favoriteUsagePointGroups = favoritesService.getFavoriteUsagePointGroups();
        List<FavoriteUsagePoint> favoriteUsagePoints = favoritesService.getFavoriteUsagePoints();
        assertThat(favoriteUsagePointGroups).isEmpty();
        assertThat(favoriteUsagePoints).isEmpty();
        threadPrincipalService.set(user1);
        favoritesService.markFavorite(usagePointGroup1);
        favoritesService.markFavorite(usagePointGroup2);
        threadPrincipalService.set(user2);
        favoritesService.markFavorite(usagePoint1);
        favoritesService.markFavorite(usagePoint2);

        favoriteUsagePointGroups = favoritesService.getFavoriteUsagePointGroups();
        favoriteUsagePoints = favoritesService.getFavoriteUsagePoints();
        assertThat(favoriteUsagePointGroups).isEmpty();
        assertThat(favoriteUsagePoints).hasSize(2);
        assertThat(favoriteUsagePoints.get(0).getUsagePoint()).isEqualTo(usagePoint1);
        assertThat(favoriteUsagePoints.get(1).getUsagePoint()).isEqualTo(usagePoint2);

        threadPrincipalService.set(user1);
        favoriteUsagePointGroups = favoritesService.getFavoriteUsagePointGroups();
        favoriteUsagePoints = favoritesService.getFavoriteUsagePoints();
        assertThat(favoriteUsagePointGroups).hasSize(2);
        assertThat(favoriteUsagePointGroups.get(0).getUsagePointGroup()).isEqualTo(usagePointGroup1);
        assertThat(favoriteUsagePointGroups.get(1).getUsagePointGroup()).isEqualTo(usagePointGroup2);
        assertThat(favoriteUsagePoints).isEmpty();

        threadPrincipalService.set(() -> "Fake!");
        assertThat(favoritesService.getFavoriteUsagePointGroups()).isEmpty();
        assertThat(favoritesService.getFavoriteUsagePoints()).isEmpty();
    }

    @Test
    @Transactional
    public void testCreateAndFindFavoriteUsagePointGroup() {
        threadPrincipalService.set(user2);
        assertThat(favoritesService.findFavoriteUsagePointGroup(usagePointGroup1)).isEmpty();

        FavoriteUsagePointGroup favoriteUsagePointGroup = favoritesService.markFavorite(usagePointGroup1);
        favoriteUsagePointGroup.updateComment("This string is empty");
        assertThat(favoriteUsagePointGroup.getUsagePointGroup()).isEqualTo(usagePointGroup1);
        assertThat(favoriteUsagePointGroup.getUser()).isEqualTo(user2);
        assertThat(favoriteUsagePointGroup.getComment()).isEqualTo("This string is empty");
        assertThat(favoriteUsagePointGroup.getCreationDate()).isEqualTo(NOW);

        favoriteUsagePointGroup = favoritesService.findFavoriteUsagePointGroup(usagePointGroup1)
                .orElseThrow(() -> new AssertionError("Favorite usage point group is created but not found afterwards"));
        assertThat(favoriteUsagePointGroup.getUsagePointGroup()).isEqualTo(usagePointGroup1);
        assertThat(favoriteUsagePointGroup.getUser()).isEqualTo(user2);
        assertThat(favoriteUsagePointGroup.getComment()).isEqualTo("This string is empty");
        assertThat(favoriteUsagePointGroup.getCreationDate()).isEqualTo(NOW);

        favoriteUsagePointGroup.updateComment("This string contains only lies");
        favoriteUsagePointGroup = favoritesService.markFavorite(usagePointGroup1);
        assertThat(favoriteUsagePointGroup.getUsagePointGroup()).isEqualTo(usagePointGroup1);
        assertThat(favoriteUsagePointGroup.getUser()).isEqualTo(user2);
        assertThat(favoriteUsagePointGroup.getComment()).isEqualTo("This string contains only lies");
        assertThat(favoriteUsagePointGroup.getCreationDate()).isEqualTo(NOW);
    }

    @Test
    @Transactional
    public void testCreateAndFindFavoriteUsagePoint() {
        threadPrincipalService.set(user1);
        assertThat(favoritesService.findFavoriteUsagePoint(usagePoint1)).isEmpty();

        FavoriteUsagePoint favoriteUsagePoint = favoritesService.markFavorite(usagePoint1);
        favoriteUsagePoint.updateComment("This string is empty");
        assertThat(favoriteUsagePoint.getUsagePoint()).isEqualTo(usagePoint1);
        assertThat(favoriteUsagePoint.getUser()).isEqualTo(user1);
        assertThat(favoriteUsagePoint.getComment()).isEqualTo("This string is empty");
        assertThat(favoriteUsagePoint.getCreationDate()).isEqualTo(NOW);

        favoriteUsagePoint = favoritesService.findFavoriteUsagePoint(usagePoint1)
                .orElseThrow(() -> new AssertionError("Favorite usage point is created but not found afterwards"));
        assertThat(favoriteUsagePoint.getUsagePoint()).isEqualTo(usagePoint1);
        assertThat(favoriteUsagePoint.getUser()).isEqualTo(user1);
        assertThat(favoriteUsagePoint.getComment()).isEqualTo("This string is empty");
        assertThat(favoriteUsagePoint.getCreationDate()).isEqualTo(NOW);

        favoriteUsagePoint.updateComment("This string contains only lies");
        favoriteUsagePoint = favoritesService.markFavorite(usagePoint1);
        assertThat(favoriteUsagePoint.getUsagePoint()).isEqualTo(usagePoint1);
        assertThat(favoriteUsagePoint.getUser()).isEqualTo(user1);
        assertThat(favoriteUsagePoint.getComment()).isEqualTo("This string contains only lies");
        assertThat(favoriteUsagePoint.getCreationDate()).isEqualTo(NOW);
    }

    @Test
    @Transactional
    public void testSameUsagePointGroupIsFavoriteForDifferentUsers() {
        threadPrincipalService.set(user1);
        favoritesService.markFavorite(usagePointGroup1).updateComment("user1");
        threadPrincipalService.set(user2);
        favoritesService.markFavorite(usagePointGroup1).updateComment("user2");
        threadPrincipalService.set(user1);
        favoritesService.findFavoriteUsagePointGroup(usagePointGroup1)
                .map(FavoriteUsagePointGroup::getComment)
                .map(comment -> assertThat(comment).isEqualTo("user1"))
                .orElseThrow(() -> new AssertionError("Favorite usage point group is created but not found afterwards"));
        threadPrincipalService.set(user2);
        favoritesService.findFavoriteUsagePointGroup(usagePointGroup1)
                .map(FavoriteUsagePointGroup::getComment)
                .map(comment -> assertThat(comment).isEqualTo("user2"))
                .orElseThrow(() -> new AssertionError("Favorite usage point group is created but not found afterwards"));
    }

    @Test
    @Transactional
    public void testSameUsagePointIsFavoriteForDifferentUsers() {
        threadPrincipalService.set(user1);
        favoritesService.markFavorite(usagePoint1).updateComment("user1");
        threadPrincipalService.set(user2);
        favoritesService.markFavorite(usagePoint1).updateComment("user2");
        threadPrincipalService.set(user1);
        favoritesService.findFavoriteUsagePoint(usagePoint1)
                .map(FavoriteUsagePoint::getComment)
                .map(comment -> assertThat(comment).isEqualTo("user1"))
                .orElseThrow(() -> new AssertionError("Favorite usage point is created but not found afterwards"));
        threadPrincipalService.set(user2);
        favoritesService.findFavoriteUsagePoint(usagePoint1)
                .map(FavoriteUsagePoint::getComment)
                .map(comment -> assertThat(comment).isEqualTo("user2"))
                .orElseThrow(() -> new AssertionError("Favorite usage point is created but not found afterwards"));
    }

    @Test
    @Transactional
    public void testRemoveFavoriteUsagePointGroup() {
        threadPrincipalService.set(user1);
        favoritesService.markFavorite(usagePointGroup1);
        favoritesService.markFavorite(usagePointGroup2);
        FavoriteUsagePointGroup favoriteUsagePointGroup = favoritesService.findFavoriteUsagePointGroup(usagePointGroup1)
                .orElseThrow(() -> new AssertionError("Favorite usage point group is created but not found afterwards"));
        favoritesService.removeFromFavorites(favoriteUsagePointGroup);

        List<FavoriteUsagePointGroup> favoriteUsagePointGroups = favoritesService.getFavoriteUsagePointGroups();
        assertThat(favoriteUsagePointGroups).hasSize(1);
        assertThat(favoriteUsagePointGroups.get(0).getUsagePointGroup()).isEqualTo(usagePointGroup2);

        assertThat(favoritesService.findFavoriteUsagePointGroup(usagePointGroup1)).isEmpty();
    }

    @Test
    @Transactional
    public void testRemoveFavoriteUsagePoint() {
        threadPrincipalService.set(user1);
        favoritesService.markFavorite(usagePoint1);
        favoritesService.markFavorite(usagePoint2);
        FavoriteUsagePoint favoriteUsagePoint = favoritesService.findFavoriteUsagePoint(usagePoint1)
                .orElseThrow(() -> new AssertionError("Favorite usage point is created but not found afterwards"));
        favoritesService.removeFromFavorites(favoriteUsagePoint);

        List<FavoriteUsagePoint> favoriteUsagePoints = favoritesService.getFavoriteUsagePoints();
        assertThat(favoriteUsagePoints).hasSize(1);
        assertThat(favoriteUsagePoints.get(0).getUsagePoint()).isEqualTo(usagePoint2);

        assertThat(favoritesService.findFavoriteUsagePoint(usagePoint1)).isEmpty();
    }
}
