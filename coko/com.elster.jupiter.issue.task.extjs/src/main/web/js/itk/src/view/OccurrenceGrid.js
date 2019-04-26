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
            text: Uni.I18n.translate('general.task.startOn', 'ITK', 'Start on'),
            dataIndex: 'startDate',
            flex: 1,
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTimeShort(value) : '';
            }
        },
        {
            text: Uni.I18n.translate('general.task.duration', 'ITK', 'Duration'),
            dataIndex: 'duration',
            xtype: 'uni-grid-column-duration',
            shortFormat: true,
            textAlign: 'center',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.failureTime', 'ITK', 'Failure on'),
            dataIndex: 'failureTime',
            flex: 1,
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTimeShort(value) : '';
            }
        },
        {
            text: Uni.I18n.translate('general.task.status', 'ITK', 'Status'),
            dataIndex: 'status',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.startDate', 'ITK', 'Message'),
            dataIndex: 'errorMessage',
            flex: 2
        }
    ]
});
