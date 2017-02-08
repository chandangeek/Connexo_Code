/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.group.GroupActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.group-action-menu',
    itemId: 'group-action-menu',
    initComponent: function() {

        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                itemId: 'editGroup',
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'USR', 'Remove'),
                itemId: 'removeGroup',
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function (menu) {
            menu.record.get('name') == 'Administrators' ? menu.down('#removeGroup').hide() : menu.down('#removeGroup').show();
        }
    }
});
