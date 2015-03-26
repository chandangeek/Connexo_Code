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

        me.callParent(arguments);
    }
});

