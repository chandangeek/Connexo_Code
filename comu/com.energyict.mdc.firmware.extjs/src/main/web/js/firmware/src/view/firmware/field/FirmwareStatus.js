/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.field.FirmwareStatus', {
    extend: 'Fwc.view.firmware.field.Radio',
    requires: [
        'Fwc.store.FirmwareStatuses'
    ],
    xtype: 'firmware-status',
    fieldLabel: Uni.I18n.translate('firmware.field.status', 'FWC', 'Firmware status'),
    columns: 1,
    vertical: true,
    name: 'firmwareStatus',
    displayField: 'localizedValue',
    valueField: 'id',
    store: 'Fwc.store.FirmwareStatuses'
});
