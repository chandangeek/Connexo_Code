/*
 *
 *  * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.elster.jupiter.users.blacklist.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_4SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_7_1SimpleUpgrader;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.blacklist.BlackListToken;
import com.elster.jupiter.users.blacklist.BlackListTokenService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import static com.elster.jupiter.orm.Version.version;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 1/3/2020 (16:45)
 */

@Component(name = "com.elster.jupiter.users.impl",
        service = {BlackListTokenService.class, MessageSeedProvider.class},
        property = {"name=" + BlackListTokenService.COMPONENTNAME,
        },
        immediate = true)
public class BlackListTokenServiceImpl implements BlackListTokenService, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile UpgradeService upgradeService;
    private volatile TransactionService transactionService;
    private volatile Clock clock;

    private Timer dailyCheck = new Timer("Token Expiry");

    public BlackListTokenServiceImpl(){

    }

    @Inject
    public BlackListTokenServiceImpl(OrmService ormService,
                                     QueryService queryService,
                                     NlsService nlsService,
                                     UpgradeService upgradeService,
                                     Clock clock,
                                     TransactionService transactionService) {
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setClock(clock);
        setTransactionService(transactionService);
        activate();
    }
    @Activate
    public void activate() {

        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }

        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(NlsService.class).toInstance(nlsService);
                bind(QueryService.class).toInstance(queryService);
                bind(BlackListTokenService.class).toInstance(BlackListTokenServiceImpl.this);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UpgradeService.class).toInstance(upgradeService);
                bind(Clock.class).toInstance(clock);
            }
        });

        this.upgradeService.register(
                InstallIdentifier.identifier("Pulse", BlackListTokenService.COMPONENTNAME),
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        version(10, 4), V10_4SimpleUpgrader.class,
                        version(10, 7, 1), V10_7_1SimpleUpgrader.class)
        );
        dailyCheck.scheduleAtFixedRate(new TokenExpiryCheckTask(), 0, 24 * 60 * 60 * 1000);
    }
    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(BlackListTokenService.COMPONENTNAME, "black_list_token");

    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(BlackListTokenService.COMPONENTNAME, Layer.DOMAIN);

    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public BlackListTokenBuilder getBlackListTokenService() {
        return new BlackListTokenBuilderImpl(dataModel.getInstance(BlackListTokenImpl.class));

    }

    @Override
    public Optional<BlackListToken> findToken(long userId, String token) {
        if(null != token && userId < 0) {
            Condition userIdCondition = Operator.EQUALIGNORECASE.compare("userId", userId);
            Condition tokenCondition = Operator.EQUALIGNORECASE.compare("token", token);
            List<BlackListToken> tokens = dataModel.query(BlackListToken.class).select(userIdCondition.and(tokenCondition));
            if (!tokens.isEmpty()) {
                return Optional.of(tokens.get(0));
            }
        }
        return  Optional.empty();
    }

    @Override
    public void deleteExpiredTokens() {
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (PreparedStatement statement = deleteSql().prepare(connection)) {
                    statement.executeUpdate();
            } catch (SQLException ex) {
                throw new UnderlyingSQLFailedException(ex);
            }
        });
    }

    SqlBuilder deleteSql() {
        String tokenExpTime = System.getProperty("com.elster.jupiter.token.expirationtime");
        final long TOKEN_EXPTIME = (tokenExpTime != null) ? Integer.parseInt(tokenExpTime) : 1800;
        Instant creationTime = Instant.now().minus(TOKEN_EXPTIME, ChronoUnit.SECONDS);
        SqlBuilder builder = new SqlBuilder("DELETE FROM ");
        builder.append(TableSpecs.BLT_BLACKLISTEDTOKEN.name());
        builder.append(" WHERE CREATETIME < ");
        builder.append(""+creationTime.getEpochSecond() * 1000);
        return builder;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }


    class TokenExpiryCheckTask extends TimerTask {
        @Override
        public void run() {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                deleteExpiredTokens();
                transactionContext.commit();
            }
        }
    }
}
