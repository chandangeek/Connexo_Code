Ext.define('Fwc.devicefirmware.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-firmware-log-grid',
    store: 'Fwc.devicefirmware.store.FirmwareLogs',
//    requires: [
//        'Uni.view.toolbar.PagingTop',
//        'Uni.view.toolbar.PagingBottom',
//        'Dlc.devicelifecyclestates.view.ActionMenu'
//    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceFirmware.log.timestamp', 'DLC', 'Timestamp'),
                dataIndex: 'timestamp',
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceFirmware.log.description', 'DLC', 'Description'),
                dataIndex: 'description',
                flex: 2
            },
            {
                header: Uni.I18n.translate('deviceFirmware.log.level', 'DLC', 'Log level'),
                dataIndex: 'level',
                flex: 1
            }
        ];

        me.callParent(arguments);
    }
});