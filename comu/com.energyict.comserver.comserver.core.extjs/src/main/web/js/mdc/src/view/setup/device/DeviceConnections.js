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
    itemId: 'connectionslist',
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
        '<td>' + Uni.I18n.translate('device.connections.comPortPool', 'MDC', 'Communication port pool') + '</td>',
        '<td>{[values.comPortPool.name]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.connections.startedOn', 'MDC', 'Started on') + '</td>',
        '<td>{[values.startDateTime]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.connections.finishedOn', 'MDC', 'Finished on') + '</td>',
        '<td>{[values.endDateTime]}</td>',
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
                    flex: 0.1
                },
                {
                    itemId: 'connectionMethod',
                    text: Uni.I18n.translate('device.connections.connectionMethod', 'MDC', 'Connection method'),
                    dataIndex: 'connectionMethod',
                    flex: 1,
                    renderer: function (val, metaData, record) {
                        var me = this;
                        metaData.tdAttr = 'data-qtip="' + Ext.htmlEncode(me.connectionTpl.apply(record.getData())) + '"';
                        return val ? val.name : ''
                    }
                },
                {
                    itemId: 'currentState',
                    text: Uni.I18n.translate('device.connections.currentState', 'MDC', 'Current state'),
                    dataIndex: 'currentState',
                    flex: 1,
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    itemId: 'latestStatus',
                    text: Uni.I18n.translate('device.connections.latestStatus', 'MDC', 'Latest status'),
                    dataIndex: 'latestStatus',
                    flex: 1,
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    itemId: 'latestResult',
                    text: Uni.I18n.translate('device.connections.latestResult', 'MDC', 'Latest result'),
                    dataIndex: 'latestResult',
                    name: 'latestResult',
                    flex: 1,
                    renderer: function (val) {
                        return val ? val.displayValue : ''

                    }
                },
                {
                    dataIndex: 'taskCount',
                    itemId: 'taskCount',
                    renderer: function (val) {
                        var template = '';
                        if (val.numberOfSuccessfulTasks || val.numberOfFailedTasks || val.numberOfIncompleteTasks) {
                            template += '<tpl><img src="/apps/dsh/resources/images/widget/running.png" title="Success" class="ct-result ct-success"><span style="position: relative; top: -3px; left: 4px">' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</span></tpl>';
                            template += '<tpl><img src="/apps/dsh/resources/images/widget/blocked.png" title="Failed" class="ct-result ct-failure" style="position: relative; left: 30px"><span style="position: relative; top: -3px; left: 34px">' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '</span></tpl>';
                            template += '<tpl><img src="/apps/dsh/resources/images/widget/stopped.png" title="Not executed" class="ct-result ct-incomplete" style="position: relative; left: 56px"><span  style="position: relative; top: -3px; left: 60px">' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</span></tpl>';
                        }
                        return template;
                    },
                    header: Uni.I18n.translate('device.connections.taskCount', 'MDC', 'Communication tasks'),
                    flex: 2
                },
                {
                    itemId: 'nextExecution',
                    text: Uni.I18n.translate('device.connections.nextExecution', 'MDC', 'Next connection'),
                    dataIndex: 'nextExecution',
                    xtype: 'datecolumn',
                    format: 'd/m/Y h:i:s',
                    flex: 1
                },
                {
                    itemId: 'startDateTime',
                    text: Uni.I18n.translate('device.connections.startDateTime', 'MDC', 'Started on'),
                    dataIndex: 'startDateTime',
                    xtype: 'datecolumn',
                    format: 'd/m/Y h:i:s',
                    flex: 1
                },
                {
                    xtype: 'uni-actioncolumn',
                    menu: {
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
