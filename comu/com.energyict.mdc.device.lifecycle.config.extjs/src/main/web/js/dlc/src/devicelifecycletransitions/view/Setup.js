/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycle-transitions-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dlc.devicelifecycletransitions.view.Grid',
        'Dlc.devicelifecycletransitions.view.Preview',
        'Dlc.main.view.SideMenu',
        'Dlc.devicelifecycletransitions.view.ActionsMenu'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'device-life-cycles-side-menu',
                    itemId: 'device-life-cycles-transitions-side-menu',
                    router: me.router
                }
            ]
        };

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.transitions', 'DLC', 'Transitions'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-life-cycle-transitions-grid',
                        itemId: 'device-life-cycles-transitions-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceLifeCycleTransitions.empty.title', 'DLC', 'No transitions found'),
                        reasons: [
                            Uni.I18n.translate('deviceLifeCycleTransitions.empty.list.item1', 'DLC', 'No transitions have been added yet')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addTransition', 'DLC', 'Add transition'),
                                href: me.router.getRoute('administration/devicelifecycles/devicelifecycle/transitions/add').buildUrl(),
                                itemId: 'add-transition-button'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'device-life-cycle-transitions-preview',
                        itemId: 'device-life-cycle-transitions-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

