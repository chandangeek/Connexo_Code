/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.workgroup.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.usr-workgroup-action-menu',
    initComponent: function() {
        this.items =  [
            {
                itemId: 'edit-workgroup',
                text: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                privileges: Usr.privileges.Users.admin,
                action: 'editWorkgroup',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove-workgroup',
                text: Uni.I18n.translate('general.remove', 'USR', 'Remove'),
                privileges: Usr.privileges.Users.admin,
                action: 'removeWorkgroup',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});