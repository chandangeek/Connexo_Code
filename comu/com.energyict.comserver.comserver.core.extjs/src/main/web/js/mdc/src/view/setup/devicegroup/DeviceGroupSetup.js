Ext.define('Mdc.view.setup.devicegroup.DeviceGroupSetup', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'deviceGroupSetup',


    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceGroupSetupPanel',
                title: Uni.I18n.translate('general.deviceGroups', 'MDC', 'Device groups'),
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'deviceGroupsGridContainer',
                        grid: {
                            xtype: 'deviceGroupsGrid',
                            itemId: 'deviceGroupsGrid'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-no-device-group',
                            title: Uni.I18n.translate('deviceGroup.empty.title', 'MDC', 'No device groups found'),
                            reasons: [
                                Uni.I18n.translate('deviceGroup.empty.list.item1', 'MDC', 'No device groups have been defined yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('deviceGroup.addDeviceGroup', 'MDC', 'Add device group'),
                                    privileges: Mdc.privileges.DeviceGroup.adminDeviceGroup,
                                    action: 'createDeviceGroupButtonFromEmptyGrid',
                                    itemId: 'createDeviceGroupButtonFromEmptyGrid'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceGroupPreview',
                            itemId: 'deviceGroupPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});



