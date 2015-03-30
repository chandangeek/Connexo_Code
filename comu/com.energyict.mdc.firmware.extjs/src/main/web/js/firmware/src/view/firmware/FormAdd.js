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
            fieldLabel: 'Version',
            allowBlank: false
        },
        {
            xtype: 'firmware-type',
            required: true
        },
        {
            xtype: 'firmware-status',
            required: true
        }
    ]
});