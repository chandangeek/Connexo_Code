Ext.define('Fwc.view.firmware.field.FirmwareStatus', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'firmware-status',
    fieldLabel: 'Firmware status',
    columns: 1,
    vertical: true,
    defaults: {
        name: 'firmwareStatus'
    },
    items: [
        {
            boxLabel: 'Final',
            inputValue: 'final',
            id: 'final'
        }, {
            boxLabel: 'Test',
            inputValue: 'test',
            id: 'test'
        }
    ]
});
