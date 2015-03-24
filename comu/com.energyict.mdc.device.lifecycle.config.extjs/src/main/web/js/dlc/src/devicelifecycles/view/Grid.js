Ext.define('Dlc.devicelifecycles.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-life-cycles-grid',
    store: 'Dlc.devicelifecycles.store.DeviceLifeCycles',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                dataIndex: 'name',
                flex: 1
            }
        ];

        me.callParent(arguments);
    }
});

