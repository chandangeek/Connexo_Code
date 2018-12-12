/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.TaskOverviewGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.task-overview-grid',
    store: 'Apr.store.Tasks',
    router: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Apr.store.Tasks'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.task', 'APR', 'Task'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.application', 'APR', 'Application'),
                dataIndex: 'application',
                flex: 1,
                renderer: function(value) {
                    if(!Ext.isEmpty(value)) {
                        return value.name;
                    } else {
                        return '-';
                    }
                }
            },
            {
                header: Uni.I18n.translate('general.queue', 'APR', 'Queue'),
                dataIndex: 'queue',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.nextRun', 'APR', 'Next run'),
                dataIndex: 'queueStatusString',
                flex: 1
            }

        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('taskOverview.pagingtoolbartop.displayMsg', 'APR', '{0} - {1} of {2} tasks'),
                displayMoreMsg: Uni.I18n.translate('taskOverview.pagingtoolbartop.displayMoreMsg', 'APR', '{0} - {1} of more than {2} tasks'),
                emptyMsg: Uni.I18n.translate('taskOverview.pagingtoolbartop.emptyMsg', 'APR', 'There are no tasks to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('taskOverview.pagingtoolbarbottom.itemsPerPage', 'APR', 'Tasks per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

