Ext.define('Fwc.devicefirmware.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-firmware-log-grid',
    store: 'Fwc.devicefirmware.store.FirmwareLogs',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceFirmware.log.timestamp', 'FWC', 'Timestamp'),
                dataIndex: 'timestamp',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                }
            },
            {
                header: Uni.I18n.translate('deviceFirmware.log.description', 'FWC', 'Description'),
                dataIndex: 'errorDetails',
                flex: 2
            },
            {
                header: Uni.I18n.translate('deviceFirmware.log.level', 'FWC', 'Log level'),
                dataIndex: 'logLevel',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                usesExactCount: true,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceFirmware.pagingtoolbartop.log.displayMsg', 'FWC', '{2} log lines')
            }
        ];

        me.callParent(arguments);
    }
});