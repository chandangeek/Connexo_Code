Ext.define('Fwc.view.firmware.FormEdit', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-edit',
    edit: true,
    hydrator: 'Fwc.form.Hydrator',
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
            name: 'firmwareType',
            renderer: function (data) {
                return data.displayValue;
            }
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Stauts',
            name: 'firmwareStatus',
            renderer: function (data) {
                return data.displayValue;
            }
        }
    ]
});