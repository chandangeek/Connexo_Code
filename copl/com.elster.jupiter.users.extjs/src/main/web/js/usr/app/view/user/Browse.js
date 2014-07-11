Ext.define('Usr.view.user.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.userBrowse',
    itemId: 'userBrowse',
    overflowY: 'auto',

    requires: [
        'Usr.view.user.List',
        'Usr.view.user.Details',
        'Ext.panel.Panel',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('user.title', 'USM', 'Users'),
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
                        title: Uni.I18n.translate('user.Browse.NoItemsFoundPanel.title', 'USR', 'No users found')
                    },
                    previewComponent: {
                        xtype: 'userDetails'
                    }
                }
            ]
        }
    ]
});