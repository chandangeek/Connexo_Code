Ext.define('Fwc.view.firmware.FirmwareVersions', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'firmware-versions',
    itemId: 'firmware-versions',
    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Fwc.view.firmware.Grid',
        'Fwc.view.firmware.FirmwareVersionsTopFilter',
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
                        xtype: 'uni-form-info-message',
                        name: 'warning',
                        title: Uni.I18n.translate('deviceFirmware.title.optionsDisabled', 'FWC', 'Firmware management is not allowed on devices of this device type.'),
                        html: Uni.I18n.translate('deviceFirmware.optionsDisabled', 'FWC', 'Devices of this type won\'t have the option to upload new firmware versions.<br>If you want to activate firmware management for this device type, click <a href="{0}"> here </a>', [this.router.getRoute('administration/devicetypes/view/firmwareoptions').buildUrl().toString()]),
                        hidden: true,
                        margin: '32 0 32 0',
                        height: 130
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
                            title: Uni.I18n.translate('firmware.empty.title', 'FWC', 'No firmware versions found'),
                            reasons: [
                                Uni.I18n.translate('firmware.empty.list.item1', 'FWC', 'No firmware versions have been added yet.'),
                                Uni.I18n.translate('firmware.empty.list.item2', 'FWC', 'The filter is too narrow.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('firmwareVersion.add', 'FWC', 'Add firmware version'),
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


