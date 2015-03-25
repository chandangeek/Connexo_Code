Ext.define('Fwc.view.firmware.FormEdit', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-edit',
    edit: true,
    items: [
        {
            xtype: 'textfield',
            name: 'firmwareVersion',
            anchor: '60%',
            required: true,
            fieldLabel: 'Version',
            allowBlank: false
        },
        {
            xtype: 'firmware-field-file',
            anchor: '60%'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Firmware type',
            name: 'firmwareType'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Stauts',
            name: 'firmwareStatus'
        }
    ]
});