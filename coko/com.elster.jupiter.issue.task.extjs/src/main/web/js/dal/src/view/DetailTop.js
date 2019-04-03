/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.DetailTop', {
    extend: 'Ext.container.Container',
    alias: 'widget.issue-detail-top',
    requires: [
        'Itk.view.ActionMenu',
        'Itk.privileges.Issue'
    ],
    layout: 'hbox',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'issue-detail-title',
                title: Uni.I18n.translate('general.details', 'ITK', 'Details'),
                ui: 'medium',
                flex: 1
            },
            {
                xtype: 'uni-button-action',
                margin: '5 0 0 0',
                itemId: 'issue-detail-top-actions-button',
                privileges: Itk.privileges.Issue.adminDevice,
                menu: {
                    xtype: 'issues-action-menu',
                    itemId: 'issue-detail-action-menu',
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