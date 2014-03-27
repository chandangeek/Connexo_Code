Ext.define('Usr.view.Home', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.Home',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
    ],

    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            border: 1,
            style: {
                borderColor: 'lightgray',
                borderStyle: 'solid'
            },
            maxWidth: 500,
            minHeight: 200,
            padding: '10 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                margins: '0 0 10 5'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('user.root', 'USM', 'User Management') + '</h1>'
                },
                {
                    xtype: 'box',
                    margins: '0 0 10 50',
                    itemId: 'usersLink',
                    autoEl: {
                        tag: 'a',
                        href: '#/users',
                        html: Uni.I18n.translate('user.title', 'USM', 'Users')
                    }
                },
                {
                    xtype: 'box',
                    margins: '0 0 10 50',
                    itemId: 'groupsLink',
                    autoEl: {
                        tag: 'a',
                        href: '#/roles',
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