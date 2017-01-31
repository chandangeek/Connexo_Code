/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
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
                            title: Uni.I18n.translate('firmware.empty.title', 'FWC', 'No firmware versions found'),
                            reasons: [
                                Uni.I18n.translate('firmware.empty.list.item1', 'FWC', 'No firmware versions have been added yet.'),
                                Uni.I18n.translate('firmware.empty.list.item2', 'FWC', 'No firmware versions comply with the filter.')
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


