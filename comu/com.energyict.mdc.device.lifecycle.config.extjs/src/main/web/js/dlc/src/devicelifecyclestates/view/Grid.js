Ext.define('Dlc.devicelifecyclestates.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-life-cycle-states-grid',
    store: 'Dlc.devicelifecyclestates.store.DeviceLifeCycleStates',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.states', 'DLC', 'States'),
                dataIndex: 'name',
                flex: 1
            }
        ];

        me.callParent(arguments);
    }
});

