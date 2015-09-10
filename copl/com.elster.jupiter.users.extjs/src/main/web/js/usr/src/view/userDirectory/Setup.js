Ext.define('Usr.view.userDirectory.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usr-user-directories-setup',
    router: null,
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Usr.view.userDirectory.ActionMenu',
        'Usr.view.userDirectory.Grid',
        'Usr.view.userDirectory.Preview'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.userDirectories', 'USR', 'User directories'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'usr-user-directories-grid',
                        itemId: 'grd-user-directories',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-user-directory',
                        title: Uni.I18n.translate('userDirectories.empty.title', 'USR', 'No user directory found'),
                        reasons: [
                            Uni.I18n.translate('userDirectories.empty.list.item1', 'USR', 'No user directories have been defined yet.'),
                            Uni.I18n.translate('userDirectories.empty.list.item2', 'USR', 'User directories exist, but you do not have permission to view them.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addUserDirectory', 'USR', 'Add user directory'),
                                privileges: Usr.privileges.Users.admin,
                                href: '#/administration/userdirectories/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'usr-user-directory-preview',
                        itemId: 'pnl-user-directory-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});