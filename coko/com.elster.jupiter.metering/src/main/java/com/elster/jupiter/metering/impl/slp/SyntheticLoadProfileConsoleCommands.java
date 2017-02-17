/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.slp;

import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileBuilder;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
@Component(name = "com.elster.jupiter.metering.slp.console", service = SyntheticLoadProfileConsoleCommands.class, property = {
        "osgi.command.scope=slp",
        "osgi.command.function=createSyntheticLoadProfile",
        "osgi.command.function=viewSyntheticLoadProfiles",
        "osgi.command.function=viewSyntheticLoadProfileValues",
}, immediate = true)
public class SyntheticLoadProfileConsoleCommands {
    private static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private volatile SyntheticLoadProfileService syntheticLoadProfileService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setEventService(SyntheticLoadProfileService syntheticLoadProfileService) {
        this.syntheticLoadProfileService = syntheticLoadProfileService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public void createSyntheticLoadProfile(String name, String intervalName, String durationName, String startTime){
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            final Instant startDate = LocalDate.from(dateTimeFormat.parse(startTime)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            SyntheticLoadProfileBuilder builder = syntheticLoadProfileService.newSyntheticLoadProfile(name);
            builder.withDescription(name);
            builder.withInterval(Duration.parse(intervalName.toUpperCase()));
            builder.withDuration(Period.parse(durationName.toUpperCase()));
            builder.withStartTime(startDate);
            builder.build();
            context.commit();
        } catch (RuntimeException e){
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void viewSyntheticLoadProfiles(){
        syntheticLoadProfileService.findSyntheticLoadProfiles().stream()
                .map(cf -> "" + cf.getName()
                        + ", started: " + DateTimeFormatter.ofPattern("dd/MMM/YYYY-HH:mm", Locale.ENGLISH).format(LocalDateTime.ofInstant(cf.getStartTime(), ZoneId.systemDefault()))
                        + ", interval: " + cf.getInterval()
                        + ", duration: " + cf.getDuration()).forEach(System.out::println);
    }

    public void viewSyntheticLoadProfileValues(String name){
        Optional<SyntheticLoadProfile> correctionFactor = syntheticLoadProfileService.findSyntheticLoadProfile(name);

        if(correctionFactor.isPresent()){
            Map<Instant, BigDecimal> returnedValues = correctionFactor.get().getValues(Range.atLeast(correctionFactor.get().getStartTime()));
            returnedValues.entrySet().stream()
                    .map(e -> "" + DateTimeFormatter.ofPattern("dd MMM ''YY - HH:mm", Locale.ENGLISH).format(LocalDateTime.ofInstant(e.getKey(), ZoneId.systemDefault())) + " : " + e.getValue())
                    .forEach(System.out::println);
        }
    }
}
