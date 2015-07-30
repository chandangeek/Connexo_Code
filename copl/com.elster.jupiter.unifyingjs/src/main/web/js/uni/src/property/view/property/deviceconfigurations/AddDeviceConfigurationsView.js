Ext.define('Uni.property.view.property.deviceconfigurations.AddDeviceConfigurationsView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.uni-add-device-configurations-view',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.property.view.property.deviceconfigurations.AddDeviceConfigurationsGrid'
    ],
    content: [
        {
            ui: 'large',
            itemId: 'uni-add-device-configurations-panel',
            title: Uni.I18n.translate('deviceconfigurations.addDeviceConfigurations', 'UNI', 'Add device configurations'),
            items: [
                {
                    xtype: 'emptygridcontainer',
                    grid: {
                        xtype: 'uni-add-device-configurations-grid',
                        itemId: 'uni-add-device-configurations-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'uni-add-device-configurations-no-items-found-panel',
                        title: Uni.I18n.translate('deviceconfigurations.empty.deviceconfiguration.title', 'UNI', 'No device configurations found'),
                        reasons: [
                            Uni.I18n.translate('deviceconfigurations.empty.list.item1', 'UNI', 'No device configurations have been added yet.'),
                            Uni.I18n.translate('deviceconfigurations.empty.list.item2', 'UNI', 'Device configurations exists, but you do not have permission to view them.')
                        ]
                    }
                }
            ]
        }
    ]
});