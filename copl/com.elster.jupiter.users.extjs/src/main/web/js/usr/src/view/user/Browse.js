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
        'Uni.view.notifications.NoItemsFoundPanel'
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
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('user.Browse.NoItemsFoundPanel.title', 'USR', 'No users found'),
                        reasons: [
                            Uni.I18n.translate('user.Browse.NoItemsFoundPanel.item', 'USR', 'An error occurred while loading the users.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'userDetails'
                    }
                }
            ]
        }
    ]
});