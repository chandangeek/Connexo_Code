/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceConnections', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-connections-list',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Default',
        'Mdc.view.setup.device.ConnectionActionMenu'
    ],
    store: null,
    connectionTpl: new Ext.XTemplate(
        '<table>',
        '<tpl>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.connections.connectionMethod', 'MDC', 'Connection method') + '</td>',
        '<td>{[values.connectionMethod.name]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.connections.direction', 'MDC', 'Direction') + '</td>',
        '<td>{[values.direction]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.connections.connectionWindow', 'MDC', 'Connection window') + '</td>',
        '<td>{[values.window]}</td>',
        '</tr>',
        // GDE: If we (still) don't know what to put here, it's better to (temporarily?) remove it
        //'<tr>',
        //'<td>' + Uni.I18n.translate('device.connections.schedule', 'MDC', 'Schedule') + '</td>',
        //'<td></td>', // todo: what value should be there?
        //'</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.connections.strategy', 'MDC', 'Strategy') + '</td>',
        '<td>{[values.connectionStrategy.displayValue]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool') + '</td>',
        '<td>{[values.comPortPool.name]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.connections.startedOn', 'MDC', 'Started on') + '</td>',
        '<td>{[Uni.DateTime.formatDateTimeLong(values.startDateTime)]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.connections.finishedOn', 'MDC', 'Finished on') + '</td>',
        '<td>{[Uni.DateTime.formatDateTimeLong(values.endDateTime)]}</td>',
        '</tr>',
        '</tpl>',
        '</table>'
    ),

    initComponent: function(){
        var me = this;
        me.columns = {
            defaults: {
                sortable: false,
                    groupable: false,
                    menuDisabled: true
            },
            items: [
                {
                    xtype: 'uni-default-column',
                    dataIndex: 'isDefault',
                    minWidth: 50,
                    flex: 9
                },
                {
                    itemId: 'connectionMethod',
                    text: Uni.I18n.translate('device.connections.connection', 'MDC', 'Connection'),
                    dataIndex: 'connectionMethod',
                    flex: 10,
                    renderer: function (val, metaData, record) {
                        var me = this;
                        metaData.tdAttr = 'data-qtip="' + Ext.htmlEncode(me.connectionTpl.apply(record.getData())) + '"';
                        return val ? Ext.String.htmlEncode(val.name) : '-'
                    }
                },
                {
                    itemId: 'latestResult',
                    text: Uni.I18n.translate('device.connections.lastConnection', 'MDC', 'Last connection'),
                    dataIndex: 'latestResult',
                    name: 'latestResult',
                    flex: 10,
                    renderer: function (value, metaData) {
                        var tooltipText = '';
                        if (value.id === 'Success') {
                            tooltipText += Uni.I18n.translate('device.connections.lastConnection.tooltip.success', 'MDC', 'No communication tasks have passed. Successful connection.');
                        } else if (value.id === 'Broken') {
                            tooltipText += Uni.I18n.translate('device.connections.lastConnection.tooltip.broken', 'MDC', 'Connection broken in the middle. Some communication tasks may have passed.');
                        } else if (value.id === 'SetupError') {
                            tooltipText += Uni.I18n.translate('device.connections.lastConnection.tooltip.setupError', 'MDC', 'No connection could be made.');
                        }
                        if (value.retries) {
                            tooltipText += ' (';
                            tooltipText += Uni.I18n.translatePlural('device.connections.lastConnection.retries', value.retries, 'MDC', 'No retries', '1 retry', '{0} retries');
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
                            'device.connections.comTasksSuccessful', value.numberOfSuccessfulTasks ? value.numberOfSuccessfulTasks : 0, 'MDC',
                            'No communication tasks successful', '1 communication task successful', '{0} communication tasks successful'
                        );
                        tooltipText += '<br>';
                        tooltipText += Uni.I18n.translatePlural(
                            'device.connections.comTasksFailed', value.numberOfFailedTasks ? value.numberOfFailedTasks : 0, 'MDC',
                            'No communication tasks failed', '1 communication task failed', '{0} communication tasks failed'
                        );
                        tooltipText += '<br>';
                        tooltipText += Uni.I18n.translatePlural(
                            'device.connections.comTasksNotCompleted', value.numberOfIncompleteTasks ? value.numberOfIncompleteTasks : 0, 'MDC',
                            'No communication tasks not completed', '1 communication task not completed', '{0} communication tasks not completed'
                        );
                        if (!Ext.isEmpty(tooltipText)) {
                            metaData.tdAttr = 'data-qtip="' + tooltipText + '"';
                        } else {
                            metaData.tdAttr = 'data-qtip=""';
                        }

                        valueText += '<tpl><span class="icon-checkmark"></span>' + (value.numberOfSuccessfulTasks ? value.numberOfSuccessfulTasks : '0') + '</tpl>';
                        valueText += '<tpl><span class="icon-cross"></span>' + (value.numberOfFailedTasks ? value.numberOfFailedTasks : '0') + '</tpl>';
                        valueText += '<tpl><span  class="icon-stop2"></span>' + (value.numberOfIncompleteTasks ? value.numberOfIncompleteTasks : '0') + '</tpl>';
                        return valueText;
                    },
                    header: Uni.I18n.translate('general.lastCommunicationTasks', 'MDC', 'Last communication tasks'),
                    flex: 15
                },
                {
                    itemId: 'currentState',
                    text: Uni.I18n.translate('device.connections.status', 'MDC', 'Status'),
                    dataIndex: 'currentState',
                    flex: 10,
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.displayValue) : '-'
                    }
                },
                {
                    itemId: 'startDateTime',
                    text: Uni.I18n.translate('device.connections.startDateTime', 'MDC', 'Started on'),
                    dataIndex: 'startDateTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                    },
                    flex: 10
                },
                {
                    itemId: 'nextExecution',
                    text: Uni.I18n.translate('device.connections.nextExecution', 'MDC', 'Next connection'),
                    dataIndex: 'nextExecution',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                    },
                    flex: 10
                },
                {
                    xtype: 'uni-actioncolumn',
                    menu: {
                        plain: true,
                        xtype: 'device-connection-action-menu',
                        itemId: 'connectionsActionMenu',
                        router: me.router
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
