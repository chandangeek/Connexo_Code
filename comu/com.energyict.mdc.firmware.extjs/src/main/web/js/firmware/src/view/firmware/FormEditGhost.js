Ext.define('Fwc.view.firmware.FormEditGhost', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-edit-ghost',
    hydrator: 'Fwc.form.Hydrator',
    edit: true,
    items: [
        {
            xtype: 'displayfield',
            name: 'firmwareVersion',
            fieldLabel: Uni.I18n.translate('firmware.field.version', 'FWC', 'Version')
        },
        {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('firmware.field.type', 'FWC', 'Firmware type'),
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