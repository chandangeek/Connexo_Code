Ext.define('Fwc.view.firmware.field.FirmwareType', {
    extend: 'Fwc.view.firmware.field.Radio',
    requires: [
        'Fwc.store.FirmwareTypes'
    ],
    xtype: 'firmware-type',
    fieldLabel: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
    columns: 1,
    vertical: true,
    name: 'firmwareType',
    displayField: 'localizedValue',
    valueField: 'id',
    store: 'Fwc.store.SupportedFirmwareTypes'
});
