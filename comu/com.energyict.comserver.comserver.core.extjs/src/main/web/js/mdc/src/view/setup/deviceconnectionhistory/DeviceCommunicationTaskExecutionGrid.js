/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceCommunicationTaskExecutionGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationTaskExecutionGrid',
    itemId: 'deviceCommunicationTaskExecutionGrid',
    requires: [
        'Mdc.view.setup.deviceconnectionhistory.DeviceCommunicationTaskGridActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Duration',
        'Uni.DateTime'
    ],
    store: 'DeviceCommunicationTaskExecutions',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'name',
                text: Uni.I18n.translate('deviceconnectionhistory.communication', 'MDC', 'Communication task'),
                dataIndex: 'name',
                flex: 2
            },
            {
                itemId: 'device',
                text: Uni.I18n.translate('deviceconnectionhistory.device', 'MDC', 'Device'),
                dataIndex: 'device',
                flex: 2,
                renderer: function(device){
                    return device!=='' ? '<a href="#/devices/'+device.name+'">' + Ext.String.htmlEncode(device.name) + '</a>' : '-';
                }
            },
            {
                itemId: 'result',
                text: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                dataIndex: 'result',
                flex: 1
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('deviceconnectionhistory.startedOn', 'MDC', 'Started on'),
                dataIndex: 'startTime',
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
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'mdc-device-communication-task-grid-action-menu'
                }
            }
        ]
    },
    initComponent: function(){
        var me=this;
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devicecommunicationhistory.pagingtoolbartop.displayMsgComTasks', 'MDC', '{0} - {1} of {2} communication tasks'),
                displayMoreMsg: Uni.I18n.translate('devicecommunicationhistory.pagingtoolbartop.displayMoreMsgComTasks', 'MDC', '{0} - {1} of more than {2} communication task'),
                emptyMsg: Uni.I18n.translate('devicecommunicationhistory.pagingtoolbartop.emptyMsgComTasks', 'MDC', 'There are no communication tasks to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('devicecommunicationhistory.pagingtoolbarbottom.itemsPerPageComTasks', 'MDC', 'Communication tasks per page'),
                dock: 'bottom'
            }
        ];
        me.callParent();
    }

})
;
