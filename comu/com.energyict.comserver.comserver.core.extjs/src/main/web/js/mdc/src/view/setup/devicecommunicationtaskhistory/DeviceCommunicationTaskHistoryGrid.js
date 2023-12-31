/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationTaskHistoryGrid',
    itemId: 'deviceCommunicationTaskHistoryGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Duration',
        'Uni.DateTime'
    ],
    store: 'DeviceCommunicationTaskHistory',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'startedOn',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.startedOn', 'MDC', 'Started on'),
                dataIndex: 'startTime',
                flex: 2,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                }
            },
            {
                xtype: 'uni-grid-column-duration',
                itemId: 'durationInSeconds',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.duration', 'MDC', 'Duration'),
                dataIndex: 'durationInSeconds',
                flex: 1,
                usesSeconds: true
            },
            {
                itemId: 'result',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.result', 'MDC', 'Result'),
                dataIndex: 'result',
                flex: 1
            },
            {
                itemId: 'comSession',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.connectionUsed', 'MDC', 'Connection used'),
                dataIndex: 'comSession',
                flex: 1,
                renderer: function(value){
                    return Ext.isEmpty(value) ? '-' : Ext.String.htmlEncode(value.connectionMethod.name);
                }
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'device-communication-task-history-action-menu'
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
                displayMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbartop.displayMsgComTasks', 'MDC', '{0} - {1} of {2} communication tasks'),
                displayMoreMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbartop.displayMoreMsgComTasks', 'MDC', '{0} - {1} of more than {2} communication tasks'),
                emptyMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbartop.emptyMsgComTasks', 'MDC', 'There are no communication tasks to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {deviceId: me.deviceId},
                    {comTaskId:me.comTaskId}
                ],
                itemsPerPageMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbarbottom.itemsPerPageComTasks', 'MDC', 'Communication tasks per page'),
                dock: 'bottom'
            }
        ];
        me.callParent();
    }

})
;
