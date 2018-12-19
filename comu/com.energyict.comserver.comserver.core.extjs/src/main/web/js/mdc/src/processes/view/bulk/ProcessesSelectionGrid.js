/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.ProcessesSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    //xtype: 'issues-selection-grid',
    xtype: 'processes-selection-grid',

    counterTextFn: function (count) {
        /*return Uni.I18n.translatePlural('general.nrOfIssues.selected', count, 'ISU',
            'No issues selected', '{0} issue selected', '{0} issues selected'
        );*/
        return Uni.I18n.translatePlural('general.nrOfProcess.selected', count, 'MDC',
                    'No processes selected', '{0} process selected', '{0} processes selected');
        //return 'No processes selected';
    },

    allLabel: 'All processes',//Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.allLabel', 'ISU', 'All issues'),
    allDescription: 'Select all processes (related to filters and grouping on the processes screen)',//Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.allDescription', 'ISU', 'Select all issues (related to filters and grouping on the issues screen)'),

    selectedLabel: 'Selected processes',//Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.selectedLabel', 'ISU', 'Selected issues'),
    selectedDescription: 'Select processes in table',//Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.selectedDescription', 'ISU', 'Select issues in table'),

    cancelHref: '#/search',

    columns: {
        items: [
            {
                itemId: 'process-grid-processId',
                header: 'Process ID',//Uni.I18n.translate('general.title.issue', 'ISU', 'Issue'),
                dataIndex: 'processId',
                flex: 1
            },
            {
                itemId: 'process-grid-name',
                header: 'Name',//Uni.I18n.translate('general.type', 'ISU', 'Type'),
                dataIndex: 'name',
                flex: 1
            },
            {
                itemId: 'process-grid-version',
                header: 'Version',//Uni.I18n.translate('general.type', 'ISU', 'Type'),
                dataIndex: 'version',
                flex: 1
            },
            {
                itemId: 'process-grid-type',
                header: 'Type',//Uni.I18n.translate('general.priority', 'ISU', 'Priority'),
                dataIndex: 'type',
                flex: 1
            },
            {
                itemId: 'process-grid-object',
                header: 'Object',//Uni.I18n.translate('general.priority', 'ISU', 'Priority'),
                dataIndex: 'objectName',
                flex: 1
            },
            {
                itemId: 'process-grid-startedOn',
                header: 'Started on',//Uni.I18n.translate('general.priority', 'ISU', 'Priority'),
                dataIndex: 'startDateDisplay',
                flex: 1
            },
            {
                xtype: 'uni-grid-column-duration',
                dataIndex: 'duration',
                shortFormat: true,
                textAlign: 'center',
                flex: 1
            },
            {
                itemId: 'process-grid-status',
                header: 'Status',//Uni.I18n.translate('general.priority', 'ISU', 'Priority'),
                dataIndex: 'statusDisplay',
                flex: 1
            },
            {
                itemId: 'process-grid-startedBy',
                header: 'Started by',//Uni.I18n.translate('general.priority', 'ISU', 'Priority'),
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
