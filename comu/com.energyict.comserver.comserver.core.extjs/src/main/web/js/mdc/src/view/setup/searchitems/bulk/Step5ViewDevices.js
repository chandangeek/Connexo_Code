Ext.define('Mdc.view.setup.searchitems.bulk.Step5ViewDevices', {
    extend: 'Ext.panel.Panel',
    requires: ['Mdc.model.Device'],
    alias: 'widget.searchitems-bulk-step5-viewdevices',
    title: Uni.I18n.translate('searchItems.bulk.step5viewdevicestitle', 'MDC', 'Step 5: Status - View devices'),
    ui: 'large',
    name: 'statusPageViewDevices',
    initComponent: function () {
        var me = this,
        store = Ext.create('Ext.data.Store', {
            model: 'Mdc.model.Device',
            data: []
        });
        me.items = [
            {
                xtype: 'panel',
                itemId: 'failuremessage',
                margin: '0 0 10 0'
            },
            {
                xtype: 'gridpanel',
                itemId: 'failuredevicesgrid',
                store: store,
                height: 355,
                columns: {
                    items: [
                        {
                            itemId: 'name',
                            header: Uni.I18n.translate('searchItems.bulk.name', 'MDC', ' Name'),
                            dataIndex: 'name',
                            flex: 1,
                            renderer: function (value) {
                                return '<a href="#devices/' + value + '">' + Ext.String.htmlEncode(value) + '</a>';
                            }
                        },
                        {
                            itemId: 'serialNumber',
                            header: Uni.I18n.translate('searchItems.bulk.serialNumber', 'MDC', ' Serial number'),
                            dataIndex: 'serialNumber',
                            flex: 1
                        },
                        {
                            itemId: 'deviceType',
                            header: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                            dataIndex: 'deviceTypeName',
                            flex: 1
                        },
                        {
                            itemId: 'deviceConfiguration',
                            header: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                            dataIndex: 'deviceConfigurationName',
                            flex: 1
                        }
                    ]
                }
            }
        ];
        me.callParent(arguments);
    }
});