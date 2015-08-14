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
        '<tr>',
        '<td>' + Uni.I18n.translate('device.connections.schedule', 'MDC', 'Schedule') + '</td>',
        '<td></td>', // todo: what value should be there?
        '</tr>',
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
                    flex: 1
                },
                {
                    itemId: 'connectionMethod',
                    text: Uni.I18n.translate('device.connections.connectionMethod', 'MDC', 'Connection method'),
                    dataIndex: 'connectionMethod',
                    flex: 10,
                    renderer: function (val, metaData, record) {
                        var me = this;
                        metaData.tdAttr = 'data-qtip="' + Ext.htmlEncode(me.connectionTpl.apply(record.getData())) + '"';
                        return val ? Ext.String.htmlEncode(val.name) : ''
                    }
                },
                {
                    itemId: 'currentState',
                    text: Uni.I18n.translate('device.connections.currentState', 'MDC', 'Current state'),
                    dataIndex: 'currentState',
                    flex: 10,
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.displayValue) : ''
                    }
                },
                {
                    itemId: 'latestStatus',
                    text: Uni.I18n.translate('device.connections.latestStatus', 'MDC', 'Latest status'),
                    dataIndex: 'latestStatus',
                    flex: 10,
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.displayValue) : ''
                    }
                },
                {
                    itemId: 'latestResult',
                    text: Uni.I18n.translate('device.connections.latestResult', 'MDC', 'Latest result'),
                    dataIndex: 'latestResult',
                    name: 'latestResult',
                    flex: 10,
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.displayValue) : ''

                    }
                },
                {
                    dataIndex: 'taskCount',
                    itemId: 'taskCount',
                    renderer: function (val,metaData) {
                        metaData.tdCls = 'communication-tasks-status';
                        var template = '';                        
                            template += '<tpl><span class="icon-checkmark"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</tpl>';
                            template += '<tpl><span class="icon-close"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '</tpl>';
                            template += '<tpl><span  class="icon-stop2"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</tpl>';                        
                        return template;
                    },
                    header: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                    flex: 20
                },
                {
                    itemId: 'nextExecution',
                    text: Uni.I18n.translate('device.connections.nextExecution', 'MDC', 'Next connection'),
                    dataIndex: 'nextExecution',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                    },
                    flex: 10
                },
                {
                    itemId: 'startDateTime',
                    text: Uni.I18n.translate('device.connections.startDateTime', 'MDC', 'Started on'),
                    dataIndex: 'startDateTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeShort(value) : '';
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
