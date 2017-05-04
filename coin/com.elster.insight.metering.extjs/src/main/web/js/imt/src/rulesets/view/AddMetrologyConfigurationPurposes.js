/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.view.AddMetrologyConfigurationPurposes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-metrology-configuration-purposes',
    requires: [
        'Cfg.view.validation.RuleSetSubMenu',
        'Est.main.view.RuleSetSideMenu',
        'Imt.rulesets.view.MetrologyConfigurationPurposeDetails',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.util.FormInfoMessage',
        'Imt.rulesets.view.AddMetrologyConfigurationPurposesGrid',
        'Imt.rulesets.view.StatesGridWithPreviewContainer'
    ],
    purposesStore: null,
    router: null,
    cancelHref: null,
    ruleSetId: null,

    initComponent: function () {
        var me = this,
            bottomToolbar,
            previewComponent;

        bottomToolbar = Ext.create('Ext.toolbar.Toolbar',{
            padding: '10 0 10 0',
            itemId: 'add-metrology-configuration-purposes-bottom-toolbar',
            items: [
                {
                    xtype: 'button',
                    itemId: 'addButton',
                    text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                    action: 'add',
                    ui: 'action',
                    disabled: true
                },
                {
                    xtype: 'button',
                    itemId: 'cancelButton',
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                    action: 'cancel',
                    ui: 'link',
                    href: me.cancelHref
                }
            ]
        });

        previewComponent = !!me.usagePointStatesStore ? {
            xtype: 'add-metrology-configuration-preview-container',
            bottomToolbar: bottomToolbar,
            usagePointStatesStore: me.usagePointStatesStore,
            detailsComponent: {
                xtype: 'metrology-configuration-purpose-details',
                itemId: 'metrology-configuration-purpose-preview',
                frame: true
            }
        } : {
            xtype: 'container',
            items: [
                {
                    xtype: 'metrology-configuration-purpose-details',
                    itemId: 'metrology-configuration-purpose-preview',
                    frame: true
                }
            ]
        };

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'add-metrology-configuration-purposes-panel',
                title: Uni.I18n.translate('general.addMetrologyConfigurationPurposes', 'IMT', 'Add metrology configuration purposes'),
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        itemId: 'add-metrology-configuration-purposes-info-message',
                        text: Uni.I18n.translate('ruleSet.addMetrologyConfigurationPurposes.infoText', 'IMT', 'Only metrology configuration purposes with at least 1 matching reading type are displayed')
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'metrology-configuration-purposes-preview-container',
                        grid: {
                            xtype: 'add-metrology-configuration-purposes-grid',
                            itemId: 'add-metrology-configuration-purposes-grid',
                            store: me.purposesStore,
                            externalBottomToolbar: bottomToolbar,
                            bottomToolbarHidden: !!me.usagePointStatesStore,
                            router: me.router,
                            cancelHref: me.cancelHref
                        },
                        emptyComponent: {
                            xtype: 'container',
                            items: [
                                {
                                    xtype: 'no-items-found-panel',
                                    itemId: 'no-purposes-to-add-found-panel',
                                    title: Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.empty.list.item', 'IMT', 'No metrology configuration purposes found'),
                                    reasons: [
                                        Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.empty.list.item1', 'IMT', 'No metrology configurations have been added yet.'),
                                        Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.empty.list.item2', 'IMT', 'There are no metrology configuration purposes that have reading types that match the rules in the rule set.'),
                                        Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.empty.list.item3', 'IMT', 'Matching metrology configuration purposes exist but you do not have permission to view them.')
                                    ]
                                },
                                {
                                    xtype: 'button',
                                    itemId: 'cancel-button',
                                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                                    action: 'cancel',
                                    ui: 'link',
                                    href: me.cancelHref
                                }
                            ]
                        },
                        previewComponent: previewComponent
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
                        xtype: me.sideMenu,
                        itemId: 'stepsMenu',
                        ruleSetId: me.ruleSetId,
                        router: me.router
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});