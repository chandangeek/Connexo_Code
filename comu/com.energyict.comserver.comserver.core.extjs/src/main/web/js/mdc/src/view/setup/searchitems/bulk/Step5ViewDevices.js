Ext.define('Mdc.view.setup.searchitems.bulk.Step5ViewDevices', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.searchitems-bulk-step5-viewdevices',
    requires: ['Mdc.model.Device'],
    title: Uni.I18n.translate('searchItems.bulk.step5viewdevicestitle', 'MDC', 'Bulk action - Step 5 of 5: Status - View devices'),
    ui: 'large',
    name: 'statusPageViewDevices',
    items: [
        {
            xtype: 'panel',
            itemId: 'failuremessage',
            margin: '0 0 10 0'
        },
        {
            xtype: 'gridpanel',
            itemId: 'failuredevicesgrid',
            store: Ext.create('Ext.data.Store', {
                model: 'Mdc.model.Device',
                data: []
            }),
            height: 355,
            columns: {
                items: [
                    {
                        itemId: 'MRID',
                        header: Uni.I18n.translate('searchItems.bulk.mrid', 'MDC', ' MRID'),
                        dataIndex: 'mRID',
                        flex: 1,
                        renderer: function (value) {
                            return '<a href="#devices/' + value + '">' + value + '</a>';
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
                        header: Uni.I18n.translate('searchItems.bulk.deviceType', 'MDC', 'Device type'),
                        dataIndex: 'deviceTypeName',
                        flex: 1
                    },
                    {
                        itemId: 'deviceConfiguration',
                        header: Uni.I18n.translate('searchItems.bulk.deviceConfig', 'MDC', 'Device configuration'),
                        dataIndex: 'deviceConfigurationName',
                        flex: 1
                    }
                ]
            }
        }
    ]
});