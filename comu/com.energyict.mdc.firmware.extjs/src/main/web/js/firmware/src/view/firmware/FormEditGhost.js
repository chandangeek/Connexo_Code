Ext.define('Fwc.view.firmware.FormEditGhost', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-edit-ghost',
    hydrator: 'Fwc.form.Hydrator',
    edit: true,
    items: [
        {
            xtype: 'displayfield',
            name: 'firmwareVersion',
            fieldLabel: 'Version'
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
            xtype: 'firmware-field-file',
            anchor: '60%'
        },
        {
            xtype: 'firmware-status',
            defaultType : 'radiofield',
            value: {id: 'final'},
            required: true
        }
    ]
});