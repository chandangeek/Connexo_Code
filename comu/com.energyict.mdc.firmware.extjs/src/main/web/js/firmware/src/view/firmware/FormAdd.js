Ext.define('Fwc.view.firmware.FormAdd', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-add',
    edit: false,
    hydrator: 'Fwc.form.Hydrator',

    items: [
        {
            xtype: 'uni-form-error-message',
            itemId: 'form-errors',
            margin: '0 0 10 0',
            anchor: '60%',
            hidden: true
        },
        {
            xtype: 'firmware-field-file',
            itemId: 'firmware-field-file',
            anchor: '60%'
        },
        {
            xtype: 'textfield',
            name: 'firmwareVersion',
            itemId: 'text-firmware-version',
            anchor: '60%',
            required: true,
            fieldLabel: Uni.I18n.translate('general.version', 'FWC', 'Version'),
            allowBlank: false
        },
        {
            xtype: 'textfield',
            itemId: 'firmwareType',
            name: 'firmwareType',
            hidden: true
        },
        {
            xtype: 'textfield',
            itemId: 'firmwareStatus',
            name: 'firmwareStatus',
            hidden: true
        },
        {
            xtype: 'displayfield',
            itemId: 'disp-firmware-type',
            fieldLabel: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
            name: 'type',
            hidden: true
        },
        {
            xtype: 'firmware-type',
            itemId: 'radio-firmware-type',
            defaultType: 'radiofield',
            value: {id: 'meter'},
            required: true
        },
        {
            xtype: 'firmware-status',
            itemId: 'radio-firmware-status',
            defaultType: 'radiofield',
            value: {id: 'final'},
            required: true
        }
    ]
});