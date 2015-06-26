Ext.define('Uni.property.view.property.deviceconfigurations.AddDeviceConfigurationsGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.uni-add-device-configurations-grid',
    store: 'Uni.property.store.PropertyDeviceConfigurations',
    cancelHref: undefined,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'deviceconfigurations.counterText',
            count,
            'UNI',
            '{0} device configurations selected'
        );
    },

    allLabel: Uni.I18n.translate('deviceconfigurations.allDeviceConfigurations', 'UNI', 'All device configurations'),
    allDescription: Uni.I18n.translate('deviceconfigurations.selectAllDeviceConfigurations', 'UNI', 'Select all device configurations'),

    selectedLabel: Uni.I18n.translate('deviceconfigurations.selectedDeviceConfigurations', 'UNI', 'Selected device configurations'),
    selectedDescription: Uni.I18n.translate('deviceconfigurations.selectDeviceConfigurations', 'UNI', 'Select device configurations in table'),

    columns: [
        {
            header: Uni.I18n.translate('deviceconfigurations.deviceConfiguration', 'UNI', 'Device configuration'),
            dataIndex: 'name',
            flex: 1,
            renderer: function (value, metaData, record) {
                return '<a href="#/administration/devicetypes/' + record.get('deviceTypeId') + '/deviceconfigurations/' + record.getId() + '">' + Ext.htmlEncode(value) + '</a>';
            }
        },
        {
            header: Uni.I18n.translate('deviceconfigurations.deviceType', 'UNI', 'Device type'),
            dataIndex: 'deviceTypeName',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.status', 'UNI', 'Status'),
            dataIndex: 'active',
            renderer: function (value) {
                return value
                    ? Uni.I18n.translate('general.active', 'UNI', 'Active')
                    : Uni.I18n.translate('general.inactive', 'UNI', 'Inactive');
            }
        }
    ]
});