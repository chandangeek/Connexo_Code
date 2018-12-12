/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.userDirectory.AddUsersSetup', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'usr-add-users-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Usr.view.userDirectory.AddUsersGrid'
    ],

    userDirectoryId: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('userDirectories.selectUsers', 'USR', 'Select users'),
                itemId: 'pnl-select-users',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        selectByDefault: false,
                        grid: {
                            xtype: 'usr-add-users-grid',
                            itemId: 'grd-add-ext-users',
                            hrefCancel: '',
                            listeners: {
                                selectionchange: {
                                    fn: Ext.bind(me.onSelectionChange, me)
                                }
                            }
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-add-no-user',
                            title: Uni.I18n.translate('userDirectories.extUsers.empty.title', 'USR', 'No users found'),
                            reasons: [
                                Uni.I18n.translate('userDirectories.extUsers.empty.list.item1', 'USR', 'All users have been added to the user directory.'),
                                Uni.I18n.translate('userDirectories.extUsers.empty.list.item2', 'USR', 'No users have been defined on the configured LDAP server.'),
                                Uni.I18n.translate('userDirectories.extUsers.empty.list.item3', 'USR', 'LDAP server connection not properly configured on the user directory.'),
                                Uni.I18n.translate('userDirectories.extUsers.empty.list.item4', 'USR', 'A network error occurred.')
                            ]
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    onSelectionChange: function (selectionModel, selected) {
        this.down('[action=addUsers]').setDisabled(!selected.length);
    }
});