/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.userDirectory.AddGroupsGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    xtype: 'usr-add-groups-grid',
    store: 'Usr.store.MgmUserDirectoryExtGroups',
    height: 310,
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.nrOfGroups.selected', count, 'USR',
            'No group selected', '{0} group selected', '{0} groups selected'
        );
    },

    columns: {
        items: [
            {
                header: Uni.I18n.translate('general.groupName', 'USR', 'Group name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.groupDescription', 'USR', 'Description'),
                dataIndex: 'description',
                flex: 2
            }
        ]
    },
    buttonAlign: 'left',
    buttons: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.add','USR','Add'),
            itemId: 'btn-add-ext-group',
            action: 'addGroups',
            disabled: true,
            ui: 'action'
        },
        {
            xtype: 'button',
            itemId: 'btn-cancel-add-ext-groups',
            text: Uni.I18n.translate('general.cancel','USR','Cancel'),
            href: '#',
            ui: 'link'
        }
    ]

});
