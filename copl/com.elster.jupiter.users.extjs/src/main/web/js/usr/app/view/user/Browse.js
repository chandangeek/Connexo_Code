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
                    // No need for Uni.view.container.EmptyGridContainer, at least one user (internal Admin) is created at system install
                    xtype: 'userList'
                },
                {
                    xtype: 'userDetails'
                }
            ]
        }
    ]
});