/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConnectionHistoryGrid',
    itemId: 'deviceConnectionHistoryGrid',
    requires: [
        'Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryGridActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Duration',
        'Uni.grid.column.Action',
        'Uni.DateTime'
    ],
    store: 'DeviceConnectionHistory',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'startedOn',
                text: Uni.I18n.translate('deviceconnectionhistory.startedOn', 'MDC', 'Started on'),
                dataIndex: 'startedOn',
                flex: 2,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                }
            },
            {
                xtype: 'uni-grid-column-duration',
                itemId: 'durationInSeconds',
                text: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                dataIndex: 'durationInSeconds',
                flex: 2,
                usesSeconds: true
            },
            {
                itemId: 'status',
                text: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function(status,metadata,rowObject){
                    return status !== '' ? '<a href="#/devices/' + encodeURIComponent(this.deviceId) + '/connectionmethods/' + this.connectionId + '/history/' + rowObject.get('id') + '/viewlog?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22Connections%22%2C%22Communications%22%5D%7D' + '">' + Ext.String.htmlEncode(status) + '</a>' : '';
                }
            },
            {
                itemId: 'result',
                text: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                dataIndex: 'result',
                flex: 1,
                renderer: function (value) {
                    if (value) {
                        return Ext.String.htmlEncode(value.displayValue);
                    }
                }
            },
            {
                dataIndex: 'comTaskCount',
                itemId: 'comTaskCount',
                renderer: function (val,metaData) {
                    metaData.tdCls = 'communication-tasks-status';
                    var template = '',
                        tooltipText = '';
                    tooltipText += Uni.I18n.translatePlural(
                        'device.connections.comTasksSuccessful', val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : 0, 'MDC',
                        'No communication tasks successful', '1 communication task successful', '{0} communication tasks successful'
                    );
                    tooltipText += '<br>';
                    tooltipText += Uni.I18n.translatePlural(
                        'device.connections.comTasksFailed', val.numberOfFailedTasks ? val.numberOfFailedTasks : 0, 'MDC',
                        'No communication tasks failed', '1 communication task failed', '{0} communication tasks failed'
                    );
                    tooltipText += '<br>';
                    tooltipText += Uni.I18n.translatePlural(
                        'device.connections.comTasksNotCompleted', val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : 0, 'MDC',
                        'No communication tasks not completed', '1 communication task not completed', '{0} communication tasks not completed'
                    );
                    if (!Ext.isEmpty(tooltipText)) {
                        metaData.tdAttr = 'data-qtip="' + tooltipText + '"';
                    } else {
                        metaData.tdAttr = 'data-qtip=""';
                    }

                    template += '<tpl><span class="icon-checkmark"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</tpl>';
                    template += '<tpl><span class="icon-cross"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '</tpl>';
                    template += '<tpl><span  class="icon-stop2"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</tpl>';
                    return template;
                },
                header: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                flex: 2
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'mdc-device-connection-history-grid-action-menu'
                }
            }
        ]
    },
    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} connections'),
                displayMoreMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} connections'),
                emptyMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.emptyMsg', 'MDC', 'There are no connections to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {deviceId: me.deviceId},
                    {connectionId:me.connectionId}
                ],
                itemsPerPageMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Connections per page'),
                dock: 'bottom'
            }
        ];
        me.callParent();
    }

})
;
