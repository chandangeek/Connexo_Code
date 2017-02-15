/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.group.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.groupBrowse',
    itemId: 'groupBrowse',
    overflowY: 'auto',

    requires: [
        'Usr.view.group.List',
        'Usr.view.group.Details',
        'Usr.view.group.GroupActionMenu',
        'Ext.panel.Panel',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.roles', 'USR', 'Roles'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'groupList'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('role.Browse.empty', 'USR', 'An error occurred while loading the roles.')
                    },
                    previewComponent: {
                        xtype: 'groupDetails'
                    }
                }
            ]
        }
    ]
});