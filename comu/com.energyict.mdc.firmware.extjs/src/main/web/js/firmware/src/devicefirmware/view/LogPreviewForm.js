Ext.define('Fwc.devicefirmware.view.LogPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-firmware-log-preview-form',

    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                fieldLabel: Uni.I18n.translate('deviceFirmware.log.timestamp', 'DLC', 'Timestamp'),
                name: 'timestamp'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceFirmware.log.description', 'DLC', 'Description'),
                name: 'description'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceFirmware.log.details', 'DLC', 'Details'),
                name: 'details'
            },
            {
                fieldLabel:  Uni.I18n.translate('deviceFirmware.log.level', 'DLC', 'Log level'),
                name: 'level'
            }
        ];

        me.callParent(arguments);
    }
});