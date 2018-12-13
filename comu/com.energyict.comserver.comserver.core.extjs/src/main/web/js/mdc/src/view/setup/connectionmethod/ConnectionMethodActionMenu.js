/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.connection-method-action-menu',
    itemId: 'connection-method-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'editConnectionMethod',
                action: 'editConnectionMethod',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'deleteConnectionMethod',
                action: 'deleteConnectionMethod',
                section: this.SECTION_REMOVE
            },
            {
                text: Uni.I18n.translate('connectionmethod.setAsDefault', 'MDC', 'Set as default'),
                itemId: 'toggleDefaultMenuItem',
                action: 'toggleDefault',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});

