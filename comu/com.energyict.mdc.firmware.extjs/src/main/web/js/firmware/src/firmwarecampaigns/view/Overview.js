/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Fwc.firmwarecampaigns.view.Grid',
        'Fwc.firmwarecampaigns.view.DetailForm'
    ],
    alias: 'widget.firmware-campaigns-overview',
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: Uni.I18n.translate('firmware.campaigns.firmwareCampaigns', 'FWC', 'Firmware campaigns'),
                ui: 'large',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'firmware-campaigns-preview-container',
                        grid: {
                            xtype: 'firmware-campaigns-grid',
                            itemId: 'firmware-campaigns-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'firmware-campaigns-empty-component',
                            title: Uni.I18n.translate('firmware.campaigns.empty.title', 'FWC', 'No firmware campaigns found'),
                            reasons: [
                                Uni.I18n.translate('firmware.campaigns.empty.list.item1', 'FWC', 'No firmware campaigns have been added yet.')
                            ],
                            stepItems: [
                                {
                                    itemId: 'firmware-campaigns-empty-add-button',
                                    text: Uni.I18n.translate('firmware.campaigns.addFirmwareCampaign', 'FWC', 'Add firmware campaign'),
                                    action: 'addFirmwareCampaign',
                                    href: me.router.getRoute('workspace/firmwarecampaigns/add').buildUrl(),
                                    privileges: Fwc.privileges.FirmwareCampaign.administrate
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'firmware-campaigns-detail-form',
                            itemId: 'firmware-campaign-preview',
                            router: me.router,
                            title: ' ',
                            frame: true,
                            isPreview: true
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});