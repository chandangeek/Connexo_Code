/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.user.UserActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.user-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'activate-user',
                text: Uni.I18n.translate('general.activate', 'USR', 'Activate'),
                action: 'activate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'deactivate-user',
                text: Uni.I18n.translate('general.deactivate', 'USR', 'Deactivate'),
                action: 'activate',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                itemId: 'editUser',
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.unlock', 'USR', 'Unlock'),
                itemId: 'unlock-user',
                action: 'unlock',
                section: this.SECTION_UNLOCK
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function (menu) {
            var activate = menu.down('#activate-user'),
                deactivate = menu.down('#deactivate-user'),
                active = menu.record.raw.active,
                locked =  menu.record.raw.isUserLocked;

            activate && activate.setVisible(!active);
            deactivate && deactivate.setVisible(active);
            menu.down('#unlock-user').setVisible(locked);
        }
    }
});
