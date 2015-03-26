Ext.define('Dlc.devicelifecycles.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-life-cycles-grid',
    store: 'Dlc.devicelifecycles.store.DeviceLifeCycles',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/devicelifecycles/devicelifecycle').buildUrl({deviceLifeCycleId: record.get('id')});
                    return '<a href="' + url + '">' + value + '</a>';
                },
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                emptyMsg: '',
                dock: 'top',
                items: [
                    '->',
                    {
                        xtype: 'button',
                        itemId: 'toolbar-button',
                        text: Uni.I18n.translate('general.addDeviceLifeCycle', 'DLC', 'Add device life cycle'),
                        href: me.router.getRoute('administration/devicelifecycles/add').buildUrl()
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

