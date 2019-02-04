/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.ZonesActionsMenuFromZone', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-zones-action-menu-from-zone',

    initComponent: function () {
        this.items = [
            {
                itemId: 'device-zone-edit-action',
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editZone',
                privileges: Cfg.privileges.Validation.adminZones
            },
            {
                itemId: 'device-zone-delete-action',
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'deleteZone',
                privileges: Cfg.privileges.Validation.adminZones
            }
        ];
        this.callParent(arguments);
    },

});
