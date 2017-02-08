/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.userDirectory.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.usr-user-directory-action-menu',
    localDomainName: 'Local',
    initComponent: function() {
        this.items =  [
            {
                itemId: 'set-as-default-user-directory',
                text: Uni.I18n.translate('userDirectories.setAsDefault', 'USR', 'Set as default'),
                privileges: Usr.privileges.Users.admin,
                action: 'setAsDefault',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'synchronize-user-directory',
                text: Uni.I18n.translate('userDirectories.synchronize', 'USR', 'Synchronize'),
                privileges: Usr.privileges.Users.admin,
                action: 'synchronizeUserDirectory',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit-user-directory',
                text: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                privileges: Usr.privileges.Users.admin,
                action: 'editUserDirectory',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove-user-directory',
                text: Uni.I18n.translate('general.remove', 'USR', 'Remove'),
                privileges: Usr.privileges.Users.admin,
                action: 'removeUserDirectory',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function (menu) {
            var me = this,
                editUserDirectory = menu.down('#edit-user-directory'),
                synchronizeUserDirectory = menu.down('#synchronize-user-directory'),
                removeUserDirectory = menu.down('#remove-user-directory'),
                setAsDefault = menu.down('#set-as-default-user-directory'),
                isEditUserDirectory = true, isSynchronizeUserDirectory = true, isRemoveUserDirectory = true, isSetAsDefault = true;

            if (menu.record.get('isDefault')) {
                isSetAsDefault = false;
                isRemoveUserDirectory = false;
            }

            if (menu.record.get('name') === me.localDomainName) {
                isEditUserDirectory = false;
                isSynchronizeUserDirectory = false;
                isRemoveUserDirectory = false;
            }

            editUserDirectory && editUserDirectory.setVisible(isEditUserDirectory);
            synchronizeUserDirectory && synchronizeUserDirectory.setVisible(isSynchronizeUserDirectory);
            removeUserDirectory && removeUserDirectory.setVisible(isRemoveUserDirectory);
            setAsDefault && setAsDefault.setVisible(isSetAsDefault);
        }
    }
});