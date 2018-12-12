/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.userDirectory.AddUsersGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    xtype: 'usr-add-users-grid',
    store: 'Usr.store.MgmUserDirectoryExtUsers',
    height: 310,
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.nrOfUsers.selected', count, 'USR',
            'No user selected', '{0} user selected', '{0} users selected'
        );
    },

    columns: {
        items: [
            {
                header: Uni.I18n.translate('general.userName', 'USR', 'User name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('userDirectories.userStatus.status', 'USR', 'Status'),
                dataIndex: 'statusDisplay',
                flex: 1
            }
        ]
    },
    buttonAlign: 'left',
    buttons: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.add','USR','Add'),
            itemId: 'btn-add-ext-user',
            action: 'addUsers',
            disabled: true,
            ui: 'action'
        },
        {
            xtype: 'button',
            itemId: 'btn-cancel-add-ext-users',
            text: Uni.I18n.translate('general.cancel','USR','Cancel'),
            href: '#',
            ui: 'link'
        }
    ]

});

