package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.Set;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 6/11/2014
 * Time: 18:29
 */
@Component(name = "com.elster.jupiter.export.impl.command", service = ExportRunCommand.class,
        property = {"osgi.command.scope=export", "osgi.command.function=runExport"}, immediate = true)
public class ExportRunCommand {

    private volatile TransactionService transactionService;
    private volatile IDataExportService dataExportService;

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setDataExportService(IDataExportService dataExportService) {
        this.dataExportService = (IDataExportService) dataExportService;
    }

    public void runExport(long taskId, int millisBetweenItems) {
        ReadingTypeDataExportTaskImpl exportTask = (ReadingTypeDataExportTaskImpl) dataExportService.findExportTask(taskId)
                .orElseThrow(() -> new IllegalArgumentException("No task with id '" + taskId + "' found"));
        try (TransactionContext context = transactionService.getContext()) {
            TaskOccurrence taskOccurrence = exportTask.getRecurrentTask().createTaskOccurrence();
            IDataExportOccurrence exportOccurrence = dataExportService.createExportOccurrence(taskOccurrence);
            exportOccurrence.start();
            IReadingTypeDataExportTask task = exportOccurrence.getTask();
            Range<Instant> exportedDataInterval = exportOccurrence.getExportedDataInterval();
            if (task.getExportItems().isEmpty()) {
                EndDeviceGroup endDeviceGroup = task.getEndDeviceGroup();
                Set<ReadingType> readingTypes = task.getReadingTypes();
                for (EndDeviceMembership endDeviceMembership : endDeviceGroup.getMembers(exportedDataInterval)) {
                    Meter endDevice = (Meter) endDeviceMembership.getEndDevice();
                    for (ReadingType readingType : readingTypes) {
                        IReadingTypeDataExportItem exportItem = task.addExportItem(endDevice, readingType.getMRID());
                        doExport(taskOccurrence, exportedDataInterval, exportItem, millisBetweenItems);
                    }
                }
            } else {
                for (ReadingTypeDataExportItem exportItem : task.getExportItems()) {
                    doExport(taskOccurrence, exportedDataInterval, (IReadingTypeDataExportItem) exportItem, millisBetweenItems);
                }
            }
            exportOccurrence.end(DataExportStatus.SUCCESS);
            exportOccurrence.persist();
            context.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doExport(TaskOccurrence taskOccurrence, Range<Instant> exportedDataInterval, IReadingTypeDataExportItem exportItem, int millisToWait) {
        String meterAndReadingType = ((Meter) exportItem.getReadingContainer()).getMRID() + " and reading type " + exportItem.getReadingTypeMRId();
        taskOccurrence.log(Level.INFO, Instant.now(), "Exported data for meter " + meterAndReadingType + " for period " + exportedDataInterval);
        exportItem.updateLastRunAndLastExported(Instant.now(), exportedDataInterval.upperEndpoint());
        exportItem.update();
        try {
            Thread.sleep(millisToWait);
        } catch (InterruptedException e) {
        }
    }
}
