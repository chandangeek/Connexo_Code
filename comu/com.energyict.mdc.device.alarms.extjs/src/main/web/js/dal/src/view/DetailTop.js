/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.DetailTop', {
    extend: 'Ext.container.Container',
    alias: 'widget.alarm-detail-top',
    requires: [
        'Dal.view.ActionMenu',
        'Dal.privileges.Alarm'
    ],
    layout: 'hbox',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'alarm-detail-title',
                title: Uni.I18n.translate('general.details', 'DAL', 'Details'),
                ui: 'medium',
                flex: 1
            },
            {
                xtype: 'uni-button-action',
                margin: '5 0 0 0',
                itemId: 'alarm-detail-top-actions-button',
                privileges: Dal.privileges.Alarm.adminDevice,
                menu: {
                    xtype: 'alarms-action-menu',
                    itemId: 'alarm-detail-action-menu',
                    router: me.router
                },
                listeners: {
                    click: function () {
                        this.showMenu();
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});