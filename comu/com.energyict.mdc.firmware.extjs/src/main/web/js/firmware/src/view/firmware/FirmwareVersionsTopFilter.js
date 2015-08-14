Ext.define('Fwc.view.firmware.FirmwareVersionsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'fwc-view-firmware-versions-topfilter',

    store: 'Fwc.store.Firmwares',

    filters: [
        {
            type: 'combobox',
            dataIndex: 'firmwareType',
            emptyText: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
            multiSelect: true,
            displayField: 'localizedValue',
            valueField: 'id',
            store: 'Fwc.store.SupportedFirmwareTypes',
            itemId: 'fwc-view-firmware-versions-topfilter-type'
        },
        {
            type: 'combobox',
            dataIndex: 'firmwareStatus',
            emptyText: Uni.I18n.translate('firmware.field.status', 'FWC', 'Firmware status'),
            multiSelect: true,
            displayField: 'localizedValue',
            valueField: 'id',
            store: 'Fwc.store.FirmwareStatuses'
        }
    ],

    showOrHideFirmwareTypeFilter: function(visible) {
        this.down('#fwc-view-firmware-versions-topfilter-type').setVisible(visible);
    }
});