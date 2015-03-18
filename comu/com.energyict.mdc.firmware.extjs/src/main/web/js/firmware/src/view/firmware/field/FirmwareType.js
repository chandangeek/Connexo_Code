Ext.define('Fwc.view.firmware.field.FirmwareType', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'firmware-type',
    fieldLabel: 'Firmware type',
    columns: 1,
    vertical: true,
    items: [
        {
            name: 'type',
            boxLabel: 'Communication firmware',
            inputValue: 'communication',
            id: 'communication'
        }, {
            name: 'type',
            boxLabel: 'Meter firmware',
            inputValue: 'meter',
            id: 'meter'
        }
    ]
});
