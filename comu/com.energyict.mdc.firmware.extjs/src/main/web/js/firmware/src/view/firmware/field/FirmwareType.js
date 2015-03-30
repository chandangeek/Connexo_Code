Ext.define('Fwc.view.firmware.field.FirmwareType', {
    extend: 'Fwc.view.firmware.field.Radio',
    requires: [
        'Fwc.store.FirmwareTypes'
    ],
    xtype: 'firmware-type',
    fieldLabel: 'Firmware type',
    columns: 1,
    vertical: true,
    name: 'firmwareType',
    displayField: 'displayValue',
    valueField: 'id',
    store: 'Fwc.store.FirmwareTypes'
});
