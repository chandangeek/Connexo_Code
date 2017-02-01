/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceCommunications', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-communications-list',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Default',
        'Mdc.view.setup.device.CommunicationActionMenu'
    ],
    store: null,
    connectionTpl: new Ext.XTemplate(
        '<table>',
        '<tpl>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.comTask', 'MDC', 'Communication task') + '</td>',
        '<td>{[Ext.String.htmlEncode(values.comTask.name)]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.connectionMethod', 'MDC', 'Connection method') + '</td>',
        '<td>{[Ext.String.htmlEncode(values.connectionMethod)]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.urgency', 'MDC', 'Urgency') + '</td>',
        '<td>{[values.urgency]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.comScheduleFrequency', 'MDC', 'Frequency') + '</td>',
        '<td>{[values.temporalExpression ? Mdc.util.ScheduleToStringConverter.convert(values.temporalExpression) : "-"]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.plannedDate', 'MDC', 'Planned date') + '</td>',
        '<td>{[values.plannedDate? Uni.DateTime.formatDateTimeLong(values.plannedDate) : "-"]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.startedOn', 'MDC', 'Started on') + '</td>',
        '<td>{[values.lastCommunicationStart ? Uni.DateTime.formatDateTimeLong(new Date(values.lastCommunicationStart)) : "-"]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.finishedOn', 'MDC', 'Finished on') + '</td>',
        '<td>{[values.successfulFinishTime ? Uni.DateTime.formatDateTimeLong(values.successfulFinishTime) : "-"]}</td>',
        '</tr>',
        '</tpl>',
        '</table>'
    ),
    initComponent: function () {
        var me = this;

        me.columns = {
            defaults: {
                sortable: false,
                menuDisabled: true
            },
            items: [
                {
                    itemId: 'comTask',
                    text: Uni.I18n.translate('device.communications.comTask', 'MDC', 'Communication task'),
                    dataIndex: 'comTask',
                    renderer: function (val, metaData, record) {
                        var me = this;
                        metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(me.connectionTpl.apply(record.getData())) + '"';
                        return val ? Ext.String.htmlEncode(val.name) : '-'
                    },
                    flex: 4
                },
                {
                    itemId: 'latestResult',
                    text: Uni.I18n.translate('device.communications.lastResult', 'MDC', 'Last result'),
                    dataIndex: 'latestResult',
                    name: 'latestResult',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.displayValue) : '-'

                    },
                    flex: 2
                },
                {
                    itemId: 'currentState',
                    text: Uni.I18n.translate('device.communications.status', 'MDC', 'Status'),
                    dataIndex: 'status',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val) : '-'
                    },
                    flex: 3
                },
                {
                    itemId: 'startTime',
                    text: Uni.I18n.translate('device.communications.startedOn', 'MDC', 'Started on'),
                    dataIndex: 'lastCommunicationStart',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                    },
                    flex: 2
                },
                {
                    itemId: 'nextCommunication',
                    text: Uni.I18n.translate('device.communications.nextCommunication', 'MDC', 'Next communication'),
                    dataIndex: 'nextCommunication',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                    },
                    flex: 2
                },
                {
                    xtype: 'uni-actioncolumn',
                    menu: {
                        plain: true,
                        xtype: 'device-communication-action-menu',
                        itemId: 'communicationsActionMenu',
                        router: me.router,
                        flex: 4
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
