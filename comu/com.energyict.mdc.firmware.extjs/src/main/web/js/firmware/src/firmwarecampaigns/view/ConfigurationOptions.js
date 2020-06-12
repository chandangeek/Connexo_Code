/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.ConfigurationOptions', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Fwc.firmwarecampaigns.view.SideMenu',
        'Fwc.firmwarecampaigns.view.DevicesGrid',
        'Fwc.firmwarecampaigns.view.DevicesStatusFilter'
    ],
    alias: 'widget.firmware-campaign-configuration',
    router: null,
    deviceType: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'firmware-campaign-side-menu',
                        itemId: 'firmware-campaign-side-menu',
                        router: me.router
                    }
                ]
            }
        ];

        me.content = [
            {
                itemId: 'firmware-campaigns-configuration-panel',
                title: Uni.I18n.translate('firmware.campaigns.configuration', 'FWC', 'Campaign configuration'),
                ui: 'large',
                xtype: 'tabpanel',
                deferredRender: false,
                items: [
                        {
                            title: Uni.I18n.translate('general.specifications', 'FWC', 'Specifications'),
                            itemId: 'mdc-options-tab',
                            items: [
                                 {
                                    xtype: 'firmware-specifications',
                                    deviceType: me.deviceType
                                 }
                            ]
                        },
                        {
                            title: Uni.I18n.translate('general.firmwareVersions', 'FWC', 'Firmware versions'),
                            itemId: 'mdc-versions-tab',
                            items: [
                                {
                                    xtype: 'uni-form-info-message',
                                    itemId: 'firmware-versions-info-message',
                                    text: Uni.I18n.translate('firmwareVersions.infoMsg', 'FWC', 'This page only represents the firmware versions at the moment of campaign creation. The actual configuration could have been changed afterwards'),
                                },
                                {
                                    xtype: 'firmware-grid',
                                    store: 'Fwc.firmwarecampaigns.store.FirmwareVersionsList',
                                    showImageIdentifierColumn: me.deviceType.get('needsImageIdentifierForFirmware'),
                                    router: me.router,
                                    scroll: 'vertical',
                                    maxHeight:402,
                                   // width: 1331,
                                    autoHeight: true,
                                    autoWidth: true,
                                    isFirmwareCampaignVersions: true,
                                }
                            ]
                        }
                ]
            }
        ];

        me.callParent(arguments);
    }
});