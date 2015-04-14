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
                fieldLabel: Uni.I18n.translate('deviceFirmware.log.timestamp', 'FWC', 'Timestamp'),
                name: 'timestamp',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                }
            },
            {
                fieldLabel: Uni.I18n.translate('deviceFirmware.log.description', 'FWC', 'Description'),
                name: 'description'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceFirmware.log.details', 'FWC', 'Details'),
                name: 'details'
            },
            {
                fieldLabel:  Uni.I18n.translate('deviceFirmware.log.level', 'FWC', 'Log level'),
                name: 'level'
            }
        ];

        me.callParent(arguments);
    }
});