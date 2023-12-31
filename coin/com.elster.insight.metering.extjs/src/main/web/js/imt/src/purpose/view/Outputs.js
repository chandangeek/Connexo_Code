/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.Outputs', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.purpose-outputs',
    itemId: 'purpose-outputs',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.purpose.view.OutputsList',
        'Imt.purpose.view.PurposeDetailsForm',
        'Imt.purpose.view.OutputsFilter',
        'Imt.purpose.view.OutputPreview'
    ],
    router: null,
    defaultPeriod: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'purpose-outputs',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                padding: '0 10 10 0',
                tools: [
                    {
                        xtype: 'uni-button-action',
                        itemId: 'purpose-actions-button',
                        privileges: Cfg.privileges.Validation.canRun() || Est.privileges.EstimationConfiguration.canEstimate(),
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
                        itemId: 'purpose-details-form',
                        router: me.router
                    },
                    {
                        xtype: 'panel',
                        ui: 'medium',
                        title: Uni.I18n.translate('outputs.list.title', 'IMT', 'Outputs'),
                        padding: 0,
                        items: [
                            {
                                xtype: 'outputs-filter',
                                itemId: 'outputs-filter',
                                defaultPeriod: me.defaultPeriod
                            },
                            {
                                xtype: 'preview-container',
                                grid: {
                                    xtype: 'outputs-list',
                                    itemId: 'outputs-list',
                                    router: me.router
                                },
                                emptyComponent: {
                                    xtype: 'no-items-found-panel',
                                    itemId: 'outputs-list-empty',
                                    title: Uni.I18n.translate('outputs.list.empty', 'IMT', 'No outputs found'),
                                    reasons: [
                                        Uni.I18n.translate('outputs.list.empty.reason1', 'IMT', 'No outputs have been configured on the purpose')
                                    ]
                                },
                                previewComponent: {
                                    xtype: 'output-preview',
                                    itemId: 'output-preview',
                                    frame: true,
                                    title: ' ',
                                    router: me.router,
                                    purpose: me.purpose
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});