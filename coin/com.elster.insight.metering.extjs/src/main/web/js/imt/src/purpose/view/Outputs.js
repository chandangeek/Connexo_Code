/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.Outputs', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.purpose-outputs',
    itemId: 'purpose-outputs',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.purpose.view.OutputsList',
        'Imt.purpose.view.PurposeDetailsForm'
    ],
    router: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'purpose-outputs',
                title: me.router.getRoute().getTitle(),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                padding: '0 10 10 0',
                tools: [
                    {
                        xtype: 'uni-button-action',
                        itemId: 'purpose-actions-button',
                        privileges: Imt.privileges.UsagePoint.canAdministrate,
                        menu: {
                            xtype: 'purpose-actions-menu',
                            itemId: 'purpose-actions-menu'
                        },
                        margin: 0
                    }
                ],

                items: [
                    {
                        xtype: 'purpose-details-form',
                        record: me.purpose,
                        usagePoint: me.usagePoint,
                        itemId: 'purpose-details-form',
                        router: me.router
                    },
                    {
                        xtype: 'panel',
                        ui: 'medium',
                        title: Uni.I18n.translate('outputs.list.title', 'IMT', 'Outputs'),
                        padding: 0,
                        items: {
                            xtype: 'emptygridcontainer',
                            title: me.router.getRoute().getTitle(),
                            grid: {
                                xtype: 'outputs-list',
                                router: me.router
                            },
                            emptyComponent: {
                                xtype: 'no-items-found-panel',
                                itemId: 'outputs-list-empty',
                                title: Uni.I18n.translate('outputs.list.empty', 'IMT', 'No outputs found'),
                                reasons: [
                                    Uni.I18n.translate('outputs.list.empty.reason1', 'IMT', 'No outputs have been configured on the purpose')
                                ]
                            }
                        }
                    }
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint,
                        purposes: me.purposes
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});