/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.OccurrenceGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.occurrence-details-grid',
    store: null,
    ui: 'medium',
    maxHeight: 408,
    columns: [
        {
            text: Uni.I18n.translate('general.taskId', 'ITK', 'Occurrence id'),
            dataIndex: 'id',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.task', 'ITK', 'Task Type'),
            dataIndex: 'recurrentTask.queue',
            flex: 2
        },
        {
            text: Uni.I18n.translate('general.triggerTime', 'ITK', 'Trigger time'),
            dataIndex: 'triggerTime',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.startDate', 'ITK', 'Started on'),
            dataIndex: 'startDate',
            flex: 2
        },
        {
            text: Uni.I18n.translate('general.endDate', 'ITK', 'Finished on'),
            dataIndex: 'startDate',
            flex: 2
        },
        {
            text: Uni.I18n.translate('general.endDate', 'ITK', 'Status'),
            dataIndex: 'status',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.errorMessage', 'ITK', 'Cause'),
            dataIndex: 'errorMessage',
            flex: 5
        },
        {
            text: Uni.I18n.translate('general.failureTime', 'ITK', 'Failure time'),
            dataIndex: 'failureTime',
            flex: 2
        }
    ]
});
