Ext.define('Fwc.view.firmware.FirmwareVersions', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'firmware-versions',
    itemId: 'firmware-versions',
    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Fwc.view.firmware.Grid',
        'Fwc.view.firmware.FirmwareVersionsTopFilter',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.view.button.SortItemButton',
    ],
    deviceType: null,

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        router: this.router,
                        deviceTypeId: this.deviceType.get('id')
                    }
                ]
            }
        ];

        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: this.router.getRoute().getTitle(),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'emptygridcontainer',
                        grid: {
                            xtype: 'firmware-grid',
                            store: 'Fwc.store.Firmwares'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'empty-panel',
                            title: Uni.I18n.translate('firmware.empty.title', 'MDC', 'No firmware versions found'),
                            reasons: [
                                Uni.I18n.translate('firmware.empty.list.item1', 'MDC', 'No firmware versions have been added yet.'),
                                Uni.I18n.translate('firmware.empty.list.item2', 'MDC', 'The filter is too narrow.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('firmware.add', 'MDC', 'Add firmware version'),
                                    itemId: 'add-firmware-button',
                                    action: 'addFirmware'
                                }
                            ]
                        }
                    }
                ],
                dockedItems: [
                    {
                        dock: 'top',
                        xtype: 'fwc-view-firmware-versions-topfilter'
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});


