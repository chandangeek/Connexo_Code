/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.slp;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileBuilder;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

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
    private volatile MeteringService meteringService;

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

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }


    public void createSyntheticLoadProfile(String name, String durationName, String startTime, String readingType){
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            final Instant startDate = LocalDate.from(dateTimeFormat.parse(startTime)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            SyntheticLoadProfileBuilder builder = syntheticLoadProfileService.newSyntheticLoadProfile(name,
                    Period.parse(durationName.toUpperCase()),
                    startDate,
                    meteringService.getReadingType(readingType).get());
            builder.withDescription(name);
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
                        + ", started: " + DefaultDateTimeFormatters.shortDate().withShortTime().build().format(cf.getStartTime())
                        + ", interval: " + cf.getInterval()
                        + ", duration: " + cf.getDuration()
                        + ", readingtype: " + cf.getReadingType().map(IdentifiedObject::getMRID).orElse("-")).forEach(System.out::println);
    }

    public void viewSyntheticLoadProfileValues(String name){
        Optional<SyntheticLoadProfile> correctionFactor = syntheticLoadProfileService.findSyntheticLoadProfile(name);

        if(correctionFactor.isPresent()){
            Map<Instant, BigDecimal> returnedValues = correctionFactor.get().getValues(Range.atLeast(correctionFactor.get().getStartTime()));
            returnedValues.entrySet().stream()
                    .map(e -> "" + DefaultDateTimeFormatters.shortDate().withShortTime().build().format(e.getKey()) + " : " + e.getValue())
                    .forEach(System.out::println);
        }
    }
}
