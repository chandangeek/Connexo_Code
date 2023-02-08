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
    store: null,
    deviceType: null,
    isFirmwareCampaignVersions: null,

    initComponent: function () {
        var me = this;
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
                            store: me.store,
                            showImageIdentifierColumn: me.deviceType.get('needsImageIdentifierForFirmware'),
                            router: this.router,
                            scroll: 'vertical',
                            maxHeight: 402,
                            autoHeight: true,
                            isFirmwareCampaignVersions: me.isFirmwareCampaignVersions
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
                ]
            }
        ];

        if (!me.isFirmwareCampaignVersions) {
            me.content['dockedItems'] = [
                {
                    dock: 'top',
                    xtype: 'fwc-view-firmware-versions-topfilter'
                }
            ]
        }

        this.callParent(arguments);
    }
});


