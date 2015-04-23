Ext.define('Fwc.view.firmware.field.FirmwareType', {
    extend: 'Fwc.view.firmware.field.Radio',
    requires: [
        'Fwc.store.FirmwareTypes'
    ],
    xtype: 'firmware-type',
    fieldLabel: Uni.I18n.translate('firmware.field.type', 'FWC', 'Firmware type'),
    columns: 1,
    vertical: true,
    name: 'firmwareType',
    displayField: 'localizedValue',
    valueField: 'id',
    store: 'Fwc.store.FirmwareTypes'
});
