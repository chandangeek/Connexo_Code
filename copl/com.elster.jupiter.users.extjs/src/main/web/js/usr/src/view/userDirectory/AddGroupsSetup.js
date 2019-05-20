/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.userDirectory.AddGroupsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'usr-add-groups-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Usr.view.userDirectory.AddGroupsGrid'
    ],

    userDirectoryId: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('userDirectories.selectGroups', 'USR', 'Select groups'),
                itemId: 'pnl-select-groups',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        selectByDefault: false,
                        grid: {
                            xtype: 'usr-add-groups-grid',
                            itemId: 'grd-add-ext-groups',
                            hrefCancel: '',
                            listeners: {
                                selectionchange: {
                                    fn: Ext.bind(me.onSelectionChange, me)
                                }
                            }
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-add-no-group',
                            title: Uni.I18n.translate('userDirectories.extGroups.empty.title', 'USR', 'No groups found'),
                            reasons: [
                                Uni.I18n.translate('userDirectories.extGroups.empty.list.item1', 'USR', 'All groups have been added to the user directory.'),
                                Uni.I18n.translate('userDirectories.extGroups.empty.list.item2', 'USR', 'No groups have been defined on the configured LDAP server.'),
                                Uni.I18n.translate('userDirectories.extGroups.empty.list.item3', 'USR', 'LDAP server connection not properly configured on the user directory.'),
                                Uni.I18n.translate('userDirectories.extGroups.empty.list.item4', 'USR', 'A network error occurred.')
                            ]
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    onSelectionChange: function (selectionModel, selected) {
        this.down('[action=addGroups]').setDisabled(!selected.length);
    }
});