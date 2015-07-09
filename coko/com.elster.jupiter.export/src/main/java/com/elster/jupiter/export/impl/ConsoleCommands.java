package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.PeriodicalScheduleExpressionParser;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TemporalExpressionParser;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.CompositeScheduleExpressionParser;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

@Component(name = "com.elster.jupiter.export.console", service = ConsoleCommands.class,
        property = {
                "osgi.command.scope=export",
                "osgi.command.function=createDataExportTask",
                "osgi.command.function=dataFormatters",
                "osgi.command.function=dataSelectors",
                "osgi.command.function=dataExportTasks",
                "osgi.command.function=setDefaultExportDir",
                "osgi.command.function=getDefaultExportDir"
        }, immediate = true)
public class ConsoleCommands {

    private volatile IDataExportService dataExportService;
    private volatile TimeService timeService;
    private volatile ScheduleExpressionParser scheduleExpressionParser;
    private volatile MeteringService meteringService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile AppService appService;

    public void createDataExportTask(String name, String dataFormatter, String exportPeriodName, long nextExecution, String scheduleExpression, long groupId, String... readingTypes) {
        threadPrincipalService.set(() -> "console");
        try (TransactionContext context = transactionService.getContext()) {
            DataExportTaskBuilder.StandardSelectorBuilder builder = dataExportService.newBuilder()
                    .setName(name)
                    .setDataFormatterName(dataFormatter)
                    .setNextExecution(Instant.ofEpochMilli(nextExecution))
                    .setScheduleExpression(parse(scheduleExpression))
                    .selectingStandard()
                    .fromExportPeriod(relativePeriodByName(exportPeriodName))
                    .fromEndDeviceGroup(endDeviceGroup(groupId));
            Arrays.stream(readingTypes)
                    .map(mrid -> meteringService.getReadingType(mrid).get())
                    .forEach(builder::fromReadingType);
            ExportTask dataExportTask = builder.endSelection().build();
            dataExportTask.save();
            context.commit();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void dataFormatters() {
        dataExportService.getAvailableFormatters().stream()
                .map(DataFormatterFactory::getName)
                .forEach(System.out::println);
    }

    public void dataSelectors() {
        dataExportService.getAvailableSelectors().stream()
                .map(DataSelectorFactory::getName)
                .forEach(System.out::println);
    }

    public void dataExportTasks() {
        dataExportService.findReadingTypeDataExportTasks().stream()
                .map(task -> task.getId() + " " + task.getName())
                .forEach(System.out::println);
    }

    public void setDefaultExportDir(String appServerName, String path) {
        AppServer appServer = appService.findAppServer(appServerName).orElseThrow(() -> new RuntimeException("Cannot set default export dir for anonymous."));

        transactionService.execute(VoidTransaction.of(() -> dataExportService.setExportDirectory(appServer, Paths.get(path).toAbsolutePath())));
    }

    public void getDefaultExportDir(String appServerName) {
        AppServer appServer = appService.findAppServer(appServerName).orElseThrow(() -> new RuntimeException("Cannot set default export dir for anonymous."));

        Optional<Path> exportDirectory = dataExportService.getExportDirectory(appServer);
        System.out.println(exportDirectory.orElse(Paths.get("").toAbsolutePath().normalize()));
    }

    private EndDeviceGroup endDeviceGroup(long groupId) {
        return meteringGroupsService.findEndDeviceGroup(groupId).orElseThrow(IllegalArgumentException::new);
    }

    private ScheduleExpression parse(String scheduleExpression) {
        return scheduleExpressionParser.parse(scheduleExpression).orElseThrow(IllegalArgumentException::new);
    }

    private RelativePeriod relativePeriodByName(String exportPeriodName) {
        return timeService.findRelativePeriodByName(exportPeriodName).orElseThrow(IllegalArgumentException::new);
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = (IDataExportService) dataExportService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        CompositeScheduleExpressionParser composite = new CompositeScheduleExpressionParser();
        composite.add(cronExpressionParser);
        composite.add(PeriodicalScheduleExpressionParser.INSTANCE);
        composite.add(new TemporalExpressionParser());
        composite.add(Never.NEVER);
        this.scheduleExpressionParser = composite;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }
}
