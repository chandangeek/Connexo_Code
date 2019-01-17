/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.ProcessesSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'processes-selection-grid',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('mdc.processgrid.bulk.nrOfProcess.selected', count, 'MDC',
                    'No processes selected', '{0} process selected', '{0} processes selected');
    },

    allLabel: Uni.I18n.translate('mdc.processgrid.bulk.allprocesses', 'MDC', 'All processes'),
    allDescription: Uni.I18n.translate('mdc.processgrid.bulk.allDescription', 'MDC', 'Select all processes (related to filters and grouping on the processes screen)'),

    selectedLabel: Uni.I18n.translate('mdc.processgrid.bulk.selectedLabel', 'MDC', 'Selected processes'),
    selectedDescription: Uni.I18n.translate('mdc.processgrid.bulk.selectedDescription', 'MDC', 'Select processes in table'),

    cancelHref: '#/search',

    columns: {
        items: [
            {
                itemId: 'process-grid-processId',
                header: Uni.I18n.translate('mdc.processgrid.bulk.processId', 'MDC', 'Process ID'),
                dataIndex: 'processId',
                flex: 1
            },
            {
                itemId: 'process-grid-name',
                header: Uni.I18n.translate('mdc.processgrid.bulk.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                itemId: 'process-grid-version',
                header: Uni.I18n.translate('mdc.processgrid.bulk.version', 'MDC', 'Version'),
                dataIndex: 'version',
                flex: 1
            },
            {
                itemId: 'process-grid-type',
                header: Uni.I18n.translate('mdc.processgrid.bulk.type', 'MDC', 'Type'),
                dataIndex: 'type',
                flex: 1
            },
            {
                itemId: 'process-grid-object',
                header: Uni.I18n.translate('mdc.processgrid.bulk.object', 'MDC', 'Object'),
                dataIndex: 'objectName',
                flex: 1
            },
            {
                itemId: 'process-grid-startedOn',
                header: Uni.I18n.translate('mdc.processgrid.bulk.startedOn', 'MDC', 'Started on'),
                dataIndex: 'startDateDisplay',
                flex: 1
            },
            {
                xtype: 'uni-grid-column-duration',
                header: Uni.I18n.translate('mdc.processgrid.bulk.duration', 'MDC', 'Duration'),
                dataIndex: 'duration',
                shortFormat: true,
                textAlign: 'center',
                flex: 1
            },
            {
                itemId: 'process-grid-status',
                header: Uni.I18n.translate('mdc.processgrid.bulk.status', 'MDC', 'Status'),
                dataIndex: 'statusDisplay',
                flex: 1
            },
            {
                itemId: 'process-grid-startedBy',
                header: Uni.I18n.translate('mdc.processgrid.bulk.startedBy', 'MDC', 'Started by'),
                dataIndex: 'startedBy',
                flex: 1
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
        this.getBottomToolbar().setVisible(false);
    }
});