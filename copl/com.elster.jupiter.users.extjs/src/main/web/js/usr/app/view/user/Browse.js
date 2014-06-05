Ext.define('Usr.view.user.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.userBrowse',
    itemId: 'userBrowse',
    overflowY: 'auto',
    requires: [
        'Usr.view.user.List',
        'Usr.view.user.Details',
        'Ext.panel.Panel'
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
                        xtype: 'component',
                        html: 'There are no items'
                    },
                    previewComponent: {
                        xtype: 'userDetails'
                    }
                }
            ]
        }
    ]
});