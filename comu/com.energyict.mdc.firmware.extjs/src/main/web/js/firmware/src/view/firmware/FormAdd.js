Ext.define('Fwc.view.firmware.FormAdd', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-add',
    edit: false,
    hydrator: 'Fwc.form.Hydrator',

    items: [
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
            fieldLabel: Uni.I18n.translate('firmware.field.version', 'FWC', 'Version'),
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
            fieldLabel: Uni.I18n.translate('firmware.field.type', 'FWC', 'Firmware type'),
            name: 'type',
            hidden: true,
            required: true
        },
        {
            xtype: 'firmware-type',
            itemId: 'radio-firmware-type',
            defaultType: 'radiofield',
            value: {id: 'communication'},
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