Ext.define('Fwc.view.firmware.FormAdd', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-add',
    edit: false,
    hydrator: 'Fwc.form.Hydrator',

    items: [
        {
            xtype: 'firmware-field-file',
            anchor: '60%'
        },
        {
            xtype: 'textfield',
            name: 'firmwareVersion',
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
            xtype: 'firmware-type',
            defaultType: 'radiofield',
            value: {id: 'communication'},
            required: true
        },
        {
            xtype: 'firmware-status',
            defaultType: 'radiofield',
            value: {id: 'final'},
            required: true
        }
    ]
});