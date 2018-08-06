/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.datavalidation.view.IssueDetailTop', {
    extend: 'Ext.container.Container',
    alias: 'widget.usagepoint-issue-detail-top',
    requires: [
        'Imt.datavalidation.view.UsagePointIssueActionMenu'
    ],
    layout: 'hbox',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'issue-detail-title',
                title: Uni.I18n.translate('general.details', 'IMT', 'Details'),
                ui: 'medium',
                flex: 1
            },
            {
                xtype: 'uni-button-action',
                margin: '5 0 0 0',
                itemId: 'usagepoint-issue-detail-top-actions-button',
                // privileges: Dal.privileges.Alarm.adminDevice,
                menu: {
                    xtype: 'usagepoint-issue-action-menu',
                    itemId: 'usagepoint-issue-detail-action-menu',
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