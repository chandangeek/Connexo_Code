Ext.define('Fwc.view.firmware.Form', {
    extend: 'Ext.form.Panel',
    xtype: 'firmware-form',
    itemId: 'firmwareForm',
    ui: 'large',
    defaults: {
        labelWidth: 150
    },
    minButtonWidth: 50,
    requires: [
        'Uni.util.FormErrorMessage',
        'Fwc.view.firmware.field.File',
        'Fwc.view.firmware.field.FirmwareType',
        'Fwc.view.firmware.field.FirmwareStatus'
    ],
    record: null,

    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        border: true,
        items: {
            xtype: 'uni-form-error-message',
            itemId: 'form-errors',
            hidden: true
        }
    }],

    initComponent: function () {
        var me = this;

        me.buttons = [
            {
                text: me.edit ? Uni.I18n.translate('general.edit', 'MDC', 'Save') : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                ui: 'action',
                action: 'saveFirmware',
                itemId: 'createEditButton'
            },
            {
                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                ui: 'link',
                itemId: 'cancelLink',
                href: me.router.getRoute('administration/devicetypes/view/firmwareversions').buildUrl()
            }
        ];

        me.callParent(arguments);
        me.loadRecord(me.record);
    }
});