Ext.define('Usr.view.userDirectory.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usr-user-directories-grid',
    store: 'Usr.store.MgmUserDirectories',
    router: null,
    requires: [
        'Uni.grid.column.Default',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                xtype: 'uni-default-column',
                dataIndex: 'isDefault',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.name', 'USR', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('userDirectories.type', 'USR', 'Type'),
                dataIndex: 'typeDisplay',
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'usr-user-directory-action-menu',
                    itemId: 'mnu-user-directory-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('userDirectories.pagingtoolbartop.displayMsg', 'USR', '{0} - {1} of {2} user directories'),
                displayMoreMsg: Uni.I18n.translate('userDirectories.pagingtoolbartop.displayMoreMsg', 'USR', '{0} - {1} of more than {2} user directories'),
                emptyMsg: Uni.I18n.translate('userDirectories.pagingtoolbartop.emptyMsg', 'USR', 'There are no user directories to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addUserDirectory', 'USR', 'Add user directory'),
                        privileges: Usr.privileges.Users.admin,
                        href: '#/administration/userdirectories/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('userDirectories.pagingtoolbarbottom.itemsPerPage', 'USR', 'User directories per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
