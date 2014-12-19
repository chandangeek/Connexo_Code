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
    itemId: 'communicationslist',
    store: null,
    connectionTpl: new Ext.XTemplate(
        '<table>',
        '<tpl>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.comTask', 'MDC', 'Communication task') + '</td>',
        '<td>{[values.comTask.name]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.connectionMethod', 'MDC', 'Connection method') + '</td>',
        '<td>{[values.connectionMethod]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.urgency', 'MDC', 'Urgency') + '</td>',
        '<td>{[values.urgency]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.comScheduleFrequency', 'MDC', 'Frequency') + '</td>',
        '<td>{[values.frequency]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.plannedDate', 'MDC', 'Planned date') + '</td>',
        '<td>{[values.plannedDate]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.startedOn', 'MDC', 'Started on') + '</td>',
        '<td>{[values.startTime]}</td>',
        '</tr>',
        '<tr>',
        '<td>' + Uni.I18n.translate('device.communications.finishedOn', 'MDC', 'Finished on') + '</td>',
        '<td>{[values.successfulFinishTime]}</td>',
        '</tr>',
        '</tpl>',
        '</table>'
    ),
    initComponent: function () {
        var me = this;

        me.columns = {
            defaults: {
                sortable: false,
                menuDisabled: true,
                flex: 1
            },
            items: [
                {
                    itemId: 'comTask',
                    text: Uni.I18n.translate('device.communications.comTask', 'MDC', 'Communication task'),
                    dataIndex: 'comTask',
                    renderer: function (val, metaData, record) {
                        var me = this;
                        metaData.tdAttr = 'data-qtip="' + Ext.htmlEncode(me.connectionTpl.apply(record.getData())) + '"';
                        return val ? val.name : ''
                    }
                },
                {
                    itemId: 'currentState',
                    text: Uni.I18n.translate('device.communications.currentState', 'MDC', 'Current state'),
                    dataIndex: 'currentState',
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    itemId: 'latestResult',
                    text: Uni.I18n.translate('device.communications.latestResult', 'MDC', 'Latest result'),
                    dataIndex: 'latestResult',
                    name: 'latestResult',
                    renderer: function (val) {
                        return val ? val.displayValue : ''

                    }
                },
                {
                    itemId: 'nextCommunication',
                    text: Uni.I18n.translate('device.communications.nextCommunication', 'MDC', 'Next communication'),
                    dataIndex: 'plannedDate',
                    xtype: 'datecolumn',
                    format: 'd/m/Y h:i:s'
                },
                {
                    itemId: 'startTime',
                    text: Uni.I18n.translate('device.communications.startedOn', 'MDC', 'Started on'),
                    dataIndex: 'startTime',
                    xtype: 'datecolumn',
                    format: 'd/m/Y h:i:s'
                },
                {
                    xtype: 'uni-actioncolumn',
                    menu: {
                        plain: true,
                        xtype: 'device-communication-action-menu',
                        itemId: 'communicationsActionMenu',
                        router: me.router
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
