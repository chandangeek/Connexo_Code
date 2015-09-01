Ext.define('Mdc.view.setup.searchitems.bulk.DevicesSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'devices-selection-grid',
    store: 'Mdc.store.DevicesBuffered',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.nrOfDevices.selected', count, 'MDC',
            'No devices selected', '{0} device selected', '{0} devices selected'
        );
    },

    allLabel: Uni.I18n.translate('searchItems.bulk.allDevices', 'MDC', 'All devices'),
    allDescription: Uni.I18n.translate('searchItems.bulk.selectMsg', 'MDC', 'Select all devices (related to filters on previous screen)'),

    selectedLabel: Uni.I18n.translate('searchItems.bulk.selectedDevices', 'MDC', 'Selected devices'),
    selectedDescription: Uni.I18n.translate('searchItems.bulk.selectedDevicesInTable', 'MDC', 'Select devices in table'),

    cancelHref: '#/search',

    radioGroupName: 'devices-selection-grid-step1',

    columns: [
        {
            itemId: 'MRID',
            header: Uni.I18n.translate('searchItems.bulk.mrid', 'MDC', ' MRID'),
            dataIndex: 'mRID',
            flex: 1,
            renderer: function (value) {
                return '<a href="#devices/' + value + '">' + Ext.String.htmlEncode(value) + '</a>';
            }
        },
        {
            itemId: 'serialNumber',
            header: Uni.I18n.translate('searchItems.bulk.serialNumber', 'MDC', ' Serial number'),
            dataIndex: 'serialNumber',
            flex: 1
        },
        {
            itemId: 'deviceType',
            header: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
            dataIndex: 'deviceTypeName',
            flex: 1
        },
        {
            itemId: 'deviceConfiguration',
            header: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
            dataIndex: 'deviceConfigurationName',
            flex: 1
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        this.getBottomToolbar().setVisible(false);
    }
});