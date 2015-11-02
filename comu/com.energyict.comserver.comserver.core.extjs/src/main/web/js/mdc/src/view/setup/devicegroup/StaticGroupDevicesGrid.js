Ext.define('Mdc.view.setup.devicegroup.StaticGroupDevicesGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.static-group-devices-grid',
    store: null,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.nrOfDevices.selected', count, 'MDC',
            'No devices selected', '{0} device selected', '{0} devices selected'
        );
    },

    allLabel: Uni.I18n.translate('deviceGroup.bulk.allDevices', 'MDC', 'All devices'),
    allDescription: Uni.I18n.translate('deviceGroup.bulk.selectMsg', 'MDC', 'Select all devices (according to search criteria)'),

    selectedLabel: Uni.I18n.translate('deviceGroup.bulk.selectedDevices', 'MDC', 'Selected devices'),
    selectedDescription: Uni.I18n.translate('deviceGroup.bulk.selectedDevicesInTable', 'MDC', 'Select devices in table'),

    bottomToolbarHidden: true,

    columns: [],
    config: {
        service: null
    }
});