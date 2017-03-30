/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconfiguration.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-logbook-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'btn-edit-device-config',
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'btn-remove-device-config',
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'delete',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
