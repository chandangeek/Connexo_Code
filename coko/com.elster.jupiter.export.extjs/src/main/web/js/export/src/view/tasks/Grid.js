/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dxp-tasks-grid',
    store: 'Dxp.store.DataExportTasks',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'DES', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/dataexporttasks/dataexporttask').buildUrl({taskId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'DES', 'Status'),
                dataIndex: 'lastExportOccurrence',
                renderer: function (value) {
                    var result;
                    if (value && value.statusDate && value.statusDate != 0) {
                        result = value.statusPrefix + ' ' + Uni.DateTime.formatDateTimeShort(new Date(value.statusDate));
                    } else if (value) {
                        result = value.statusPrefix
                    } else {
                        result = Uni.I18n.translate('general.created', 'DES', 'Created');
                    }
                    return result;
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.nextRun', 'DES', 'Next run'),
                dataIndex: 'nextRun',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : Uni.I18n.translate('general.notScheduled', 'DES', 'Not scheduled');
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.suspended', 'DES', 'Suspended'),
                dataIndex: 'suspendUntilExport',
                renderer: function(value) {
                    return value ? Uni.I18n.translate('general.suspended.yes', 'DES', 'Yes') : Uni.I18n.translate('general.suspended.no', 'DES', 'No');
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'dxp-tasks-action-menu',
                    itemId: 'dxp-tasks-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('exportTasks.pagingtoolbartop.displayMsg', 'DES', '{0} - {1} of {2} export tasks'),
                displayMoreMsg: Uni.I18n.translate('exportTasks.pagingtoolbartop.displayMoreMsg', 'DES', '{0} - {1} of more than {2} export tasks'),
                emptyMsg: Uni.I18n.translate('exportTasks.pagingtoolbartop.emptyMsg', 'DES', 'There are no export tasks to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addExportTask', 'DES', 'Add export task'),
                        privileges: Dxp.privileges.DataExport.admin,
                        href: '#/administration/dataexporttasks/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('exportTasks.pagingtoolbarbottom.itemsPerPage', 'DES', 'Export tasks per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
