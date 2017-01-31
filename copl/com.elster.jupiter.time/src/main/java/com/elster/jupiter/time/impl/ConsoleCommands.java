/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativeOperation;
import com.elster.jupiter.time.RelativeOperator;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.time.console", service = ConsoleCommands.class, property = {"osgi.command.scope=time", "osgi.command.function=createRelativePeriod", "osgi.command.function=createRelativePeriodCategory", "osgi.command.function=relativePeriods", "osgi.command.function=relativePeriodCategories"}, immediate = true)
public class ConsoleCommands {

    private static final Pattern PATTERN = Pattern.compile("([A-Z_]+)([\\+\\-\\=])(\\d+)");

    private volatile TimeService timeService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    public RelativePeriodCategory createRelativePeriodCategory(String key) {
        threadPrincipalService.set(() -> "console");
        try (TransactionContext context = transactionService.getContext()) {
            RelativePeriodCategory relativePeriodCategory = timeService.createRelativePeriodCategory(key);
            context.commit();
            return relativePeriodCategory;
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void relativePeriods() {
        timeService.getRelativePeriods().stream()
                .map(RelativePeriod::getName)
                .forEach(System.out::println);
    }

    public void relativePeriodCategories() {
        timeService.getRelativePeriodCategories().stream()
                .map(RelativePeriodCategory::getName)
                .forEach(System.out::println);
    }

    private static <T> Function<Optional<T>, T> unpackOrException() {
        return new Function<Optional<T>, T>() {

            @Override
            public T apply(Optional<T> optional) {
                return optional.orElseThrow(IllegalArgumentException::new);
            }
        };
    }

    public void createRelativePeriod(Object... stuff) {
        System.out.println("public void createRelativePeriod(String name, String from, String to, String... categories)");
    }

    public void createRelativePeriod(String name, String from, String to, String... categories) {
        threadPrincipalService.set(() -> "console");
        try (TransactionContext context = transactionService.getContext()) {
            List<RelativePeriodCategory> periodCategories = Arrays.stream(categories)
                    .map(timeService::findRelativePeriodCategoryByName)
                    .map(unpackOrException())
                    .collect(Collectors.toList());

            timeService.createRelativePeriod(name, asRelativeDate(from), asRelativeDate(to), periodCategories);
            context.commit();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    private RelativeDate asRelativeDate(String encoded) {
        return Pattern.compile(",").splitAsStream(encoded)
                .map(PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> new RelativeOperation(RelativeField.valueOf(m.group(1)), RelativeOperator.from(m.group(2)), Integer.valueOf(m.group(3))))
                .collect(RelativeDate.collect());
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }
}
