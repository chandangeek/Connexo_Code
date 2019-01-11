/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Tou.view.Grid',
        'Tou.view.DetailForm'
    ],
    alias: 'widget.tou-campaigns-overview',
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: 'ToU campaigns',
                ui: 'large',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'tou-preview-container',
                        grid: {
                            xtype: 'tou-campaigns-grid',
                            itemId: 'tou-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'tou-empty-component',
                            title: 'No ToU campaigns found',
                            reasons: [
                                'No ToU campaigns have been added yet.'
                            ],
                            stepItems: [
                                {
                                    itemId: 'tou-empty-add-button',
                                    text: 'Add ToU campaign',
                                    action: 'addTouCampaign',
                                    href: me.router.getRoute('workspace/toucampaigns/add').buildUrl(),
                                    privileges: Fwc.privileges.FirmwareCampaign.administrate
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'tou-campaigns-detail-form',
                            itemId: 'tou-preview',
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