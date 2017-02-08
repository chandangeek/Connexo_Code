/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.ConnectionsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.connections-list',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Yfn.privileges.Yellowfin',
        'Dsh.view.widget.ConnectionActionMenu',
        'Uni.util.Common'
    ],
    itemId: 'connectionslist',
    store: 'Dsh.store.ConnectionTasks',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'Device',
                text: Uni.I18n.translate('general.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function (val) {
                    return  (Mdc.privileges.Device.canView() || Mdc.privileges.Device.canAdministrateDeviceData())
                        ? '<a href="#/devices/' + Uni.util.Common.encodeURIComponent(val.name) + '">' + Ext.String.htmlEncode(val.name) + '</a>' : Ext.String.htmlEncode(val.name)
                }
            },
            {
                itemId: 'connectionMethod',
                text: Uni.I18n.translate('general.connection', 'DSH', 'Connection'),
                dataIndex: 'connectionMethod',
                flex: 1,
                renderer: function (val) {
                    return val ? Ext.String.htmlEncode(val.name) : '-'
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('general.lastConnection', 'DSH', 'Last connection'),
                dataIndex: 'latestResult',
                name: 'latestResult',
                flex: 1,
                renderer: function (value, metaData) {
                    var lastResultId = value.id,
                        tooltipText = '';

                    if (lastResultId === 'Success') {
                        tooltipText += Uni.I18n.translate('connection.widget.tooltip.success', 'DSH', 'No communication tasks have passed. Successful connection.');
                    } else if (lastResultId === 'Broken') {
                        tooltipText += Uni.I18n.translate('connection.widget.tooltip.broken', 'DSH', 'Connection broken in the middle. Some communication tasks may have passed.');
                    } else if (lastResultId === 'SetupError') {
                        tooltipText += Uni.I18n.translate('connection.widget.tooltip.setupError', 'DSH', 'No connection could be made.')
                    }
                    if (value.retries) {
                        tooltipText += ' (';
                        tooltipText += Uni.I18n.translatePlural('connection.widget.details.retries', value.retries, 'DSH',
                            'No retries', '1 retry', '{0} retries'
                        );
                        tooltipText += ')';
                    }
                    if (!Ext.isEmpty(tooltipText)) {
                        metaData.tdAttr = 'data-qtip="' + tooltipText + '"';
                    } else {
                        metaData.tdAttr = 'data-qtip=""';
                    }
                    return value ? Ext.String.htmlEncode(value.displayValue) : '-'
                }
            },
            {
                dataIndex: 'taskCount',
                itemId: 'taskCount',
                renderer: function (value, metaData) {
                    metaData.tdCls = 'communication-tasks-status';
                    var valueText = '',
                        tooltipText = '';

                    tooltipText += Uni.I18n.translatePlural(
                        'connection.widget.details.comTasksSuccessful', value.numberOfSuccessfulTasks ? value.numberOfSuccessfulTasks : 0, 'DSH',
                        'No communication tasks successful', '1 communication task successful', '{0} communication tasks successful'
                    );
                    tooltipText += '<br>';
                    tooltipText += Uni.I18n.translatePlural(
                        'connection.widget.details.comTasksFailed', value.numberOfFailedTasks ? value.numberOfFailedTasks : 0, 'DSH',
                        'No communication tasks failed', '1 communication task failed', '{0} communication tasks failed'
                    );
                    tooltipText += '<br>';
                    tooltipText += Uni.I18n.translatePlural(
                        'connection.widget.details.comTasksNotCompleted', value.numberOfIncompleteTasks ? value.numberOfIncompleteTasks : 0, 'DSH',
                        'No communication tasks not completed', '1 communication task not completed', '{0} communication tasks not completed'
                    );
                    if (!Ext.isEmpty(tooltipText)) {
                        metaData.tdAttr = 'data-qtip="' + tooltipText + '"';
                    } else {
                        metaData.tdAttr = 'data-qtip=""';
                    }
                    if (value.numberOfSuccessfulTasks || value.numberOfFailedTasks || value.numberOfIncompleteTasks) {
                        valueText += '<tpl><span class="icon-checkmark"></span>' + (value.numberOfSuccessfulTasks ? value.numberOfSuccessfulTasks : '0') + '</tpl>';
                        valueText += '<tpl><span class="icon-cross"></span>' + (value.numberOfFailedTasks ? value.numberOfFailedTasks : '0') + '</tpl>';
                        valueText += '<tpl><span  class="icon-stop2"></span>' + (value.numberOfIncompleteTasks ? value.numberOfIncompleteTasks : '0') + '</tpl>';
                    }
                    return valueText;
                },
                header: Uni.I18n.translate('connection.widget.details.lastCommTasks', 'DSH', 'Last communication tasks'),
                flex: 2
            },
            {
                itemId: 'latestStatus',
                text: Uni.I18n.translate('general.lastResult', 'DSH', 'Last result'),
                dataIndex: 'latestStatus',
                flex: 1,
                renderer: function (value) {
                    return value ? Ext.String.htmlEncode(value.displayValue) : '-'
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('general.status', 'DSH', 'Status'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    return val ? Ext.String.htmlEncode(val.displayValue) : '-'
                }
            },
            {
                itemId: 'startDateTime',
                text: Uni.I18n.translate('general.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startDateTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                },
                flex: 1
            },
            {
                itemId: 'connectionsActionMenu',
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'connection-action-menu',
                    itemId: 'connectionsDetailsActionMenu'
                }
            }
        ]
    },

    dockedItems: [
        {
            itemId: 'pagingtoolbartop',
            xtype: 'pagingtoolbartop',
            dock: 'top',
            store: 'Dsh.store.ConnectionTasks',
            displayMsg: Uni.I18n.translate('connection.widget.details.displayMsg', 'DSH', '{0} - {1} of {2} connections'),
            displayMoreMsg: Uni.I18n.translate('connection.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} connections'),
            emptyMsg: Uni.I18n.translate('connection.widget.details.emptyMsg', 'DSH', 'There are no connections to display'),
            items:[
                {
                    xtype:'button',
                    itemId:'generate-report',
                    privileges: Yfn.privileges.Yellowfin.view,
                    text:Uni.I18n.translate('generatereport.generateReportButton', 'DSH', 'Generate report')
                },
                {
                    xtype:'button',
                    itemId:'btn-connections-bulk-action',
                    privileges: Mdc.privileges.Device.viewOrAdministrateOrOperateDeviceCommunication,
                    text: Uni.I18n.translate('general.bulkAction', 'DSH', 'Bulk action')
                }
            ]
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            store: 'Dsh.store.ConnectionTasks',
            dock: 'bottom',
            deferLoading: true,
            itemsPerPageMsg: Uni.I18n.translate('connection.widget.details.itemsPerPage', 'DSH', 'Connections per page')
        }
    ]
});
