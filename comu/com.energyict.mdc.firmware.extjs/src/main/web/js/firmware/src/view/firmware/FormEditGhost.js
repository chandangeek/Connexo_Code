Ext.define('Fwc.view.firmware.FormEditGhost', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-edit-ghost',
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
            name: 'firmwareType'
        },
        {
            xtype: 'firmware-field-file',
            anchor: '60%'
        },
        {
            xtype: 'firmware-status',
            required: true
        }
    ]
});