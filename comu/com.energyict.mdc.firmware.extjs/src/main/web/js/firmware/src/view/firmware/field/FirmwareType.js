Ext.define('Fwc.view.firmware.field.FirmwareType', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'firmware-type',
    fieldLabel: 'Firmware type',
    columns: 1,
    vertical: true,
    defaults: {
        name: 'firmwareType'
    },
    items: [
        {
            boxLabel: 'Communication firmware',
            inputValue: 'communication',
            id: 'communication'
        }, {
            name: 'firmwareType',
            boxLabel: 'Meter firmware',
            inputValue: 'meter',
            id: 'meter'
        }
    ]
});
