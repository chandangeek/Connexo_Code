/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycle-states-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dlc.devicelifecyclestates.view.Grid',
        'Dlc.devicelifecyclestates.view.Preview',
        'Dlc.main.view.SideMenu'
    ],

    router: null,
    lifecycleRecord: null,

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'device-life-cycles-side-menu',
                    itemId: 'states-side-menu',
                    router: me.router
                }
            ]
        };

        me.content = {
            ui: 'large',
            title: me.lifecycleRecord.get('name'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-life-cycle-states-grid',
                        itemId: 'states-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-states-panel',
                        title: Uni.I18n.translate('deviceLifeCycleStates.empty.title', 'DLC', 'No states found'),
                        reasons: [
                            Uni.I18n.translate('deviceLifeCycleStates.empty.list.item1', 'DLC', 'No states have been added yet')
                        ],
                        stepItems: [
                            {
                                xtype: 'button',
                                itemId: 'add-state-button',
                                text: Uni.I18n.translate('deviceLifeCycleStates.add', 'DLC', 'Add state'),
                                action: 'addState',
                                dynamicPrivilege: Dlc.dynamicprivileges.DeviceLifeCycle.viable
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'device-life-cycle-states-preview',
                        itemId: 'device-life-cycle-states-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

