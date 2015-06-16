Ext.define('Fwc.view.firmware.FirmwareVersions', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'firmware-versions',
    itemId: 'firmware-versions',
    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Fwc.view.firmware.Grid',
        'Fwc.view.firmware.SideFilter',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.view.button.SortItemButton'
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
                    },
                    {
                        xtype: 'firmware-side-filter',
                        itemId: 'side-filter',
                        router: this.router
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
                        xtype: 'filter-top-panel',
                        itemId: 'firmware-filter-top'
                    },
                    {
                        xtype: 'uni-form-info-message',
                        name: 'warning',
                        title: 'Firmware management is not allowed on devices of this device type.',
                        text: 'Devices of this type won\'t have the option to upload new firmware versions.<br>If you want to activate firmware management for this device type, click <a href="'
                        + this.router.getRoute('administration/devicetypes/view/firmwareoptions').buildUrl()
                        + '">here</a>.',
                        hidden: true,
                        margin: '0 0 32 0'
                    },
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
                ]
            }
        ];

        this.callParent(arguments);
    }
});


