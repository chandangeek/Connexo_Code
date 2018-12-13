/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecycletransitions.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-life-cycle-transitions-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.usagepointlifecycletransitions.view.Grid',
        'Imt.usagepointlifecycletransitions.view.Preview',
        'Imt.usagepointlifecycle.view.SideMenu',
        'Imt.usagepointlifecycletransitions.view.ActionsMenu'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'usagepoint-life-cycles-side-menu',
                    itemId: 'usagepoint-life-cycles-transitions-side-menu',
                    router: me.router
                }
            ]
        };

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.transitions', 'IMT', 'Transitions'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'usagepoint-life-cycle-transitions-grid',
                        itemId: 'usagepoint-life-cycles-transitions-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-items-found-panel',
                        title: Uni.I18n.translate('usagePointLifeCycleTransitions.empty.title', 'IMT', 'No transitions found'),
                        reasons: [
                            Uni.I18n.translate('usagePointLifeCycleTransitions.empty.list.item1', 'IMT', 'No transitions have been added yet')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addTransition', 'IMT', 'Add transition'),
                                href: me.router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/transitions/add').buildUrl(),
                                itemId: 'add-transition-button'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'usagepoint-life-cycle-transitions-preview',
                        itemId: 'usagepoint-life-cycle-transitions-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

