Ext.define('Usr.view.Home', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.Home',
    //cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
    ],

    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            padding: '0 10 0 10',
            items: [
                /*{
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('user.root', 'USM', 'User Management') + '</h1>',
                    itemId: 'usmHomePageTitle'
                },*/
                {
                    xtype: 'box',
                    margins: '0 0 10 50',
                    itemId: 'usersLink',
                    autoEl: {
                        tag: 'a',
                        href: '#usermanagement/users',
                        html: Uni.I18n.translate('user.title', 'USM', 'Users')
                    }
                },
                {
                    xtype: 'box',
                    margins: '0 0 10 50',
                    itemId: 'groupsLink',
                    autoEl: {
                        tag: 'a',
                        href: '#usermanagement/roles',
                        html: Uni.I18n.translate('group.title', 'USM', 'Roles')
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});