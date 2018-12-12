/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.workgroup.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usr-workgroups-setup',
    router: null,
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Usr.view.workgroup.ActionMenu',
        'Usr.view.workgroup.Grid',
        'Usr.view.workgroup.Preview'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.workgroups', 'USR', 'Workgroups'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'usr-workgroups-grid',
                        itemId: 'grd-workgroups',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-workgroups',
                        title: Uni.I18n.translate('workgroups.empty.title', 'USR', 'No workgroups found'),
                        reasons: [
                            Uni.I18n.translate('workgroups.empty.list.item1', 'USR', 'No workgroups have been defined yet.'),
                            Uni.I18n.translate('workgroups.empty.list.item2', 'USR', 'Workgroups exist, but you do not have permission to view them.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addWorkgroup', 'USR', 'Add workgroup'),
                                privileges: Usr.privileges.Users.admin,
                                href: '#/administration/workgroups/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'usr-workgroup-preview',
                        itemId: 'pnl-workgroups-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});