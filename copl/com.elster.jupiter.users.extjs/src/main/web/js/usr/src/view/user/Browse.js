/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.user.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.userBrowse',
    itemId: 'userBrowse',
    overflowY: 'auto',

    requires: [
        'Usr.view.user.List',
        'Usr.view.user.Details',
        'Usr.view.user.UserActionMenu',
        'Ext.panel.Panel',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.users', 'USR', 'Users'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'userList'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('user.Browse.empty', 'USR', 'An error occurred while loading the users.')
                    },
                    previewComponent: {
                        xtype: 'userDetails'
                    }
                }
            ]
        }
    ]
});