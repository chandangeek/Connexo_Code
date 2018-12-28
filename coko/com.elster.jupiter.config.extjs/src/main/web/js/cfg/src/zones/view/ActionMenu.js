/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.zones.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.zones-action-menu',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'zone-edit-action',
                text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                action: 'editZone',
                privileges: Cfg.privileges.Validation.adminZones
            },
            {
                itemId: 'zone-delete-action',
                text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
                action: 'deleteZone',
                privileges: Cfg.privileges.Validation.adminZones
            }
        ];
        me.callParent(arguments);
    }
});