/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comservercomports.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.comServerComPortsActionMenu',
    itemId: 'comServerComPortsActionMenu',

    initComponent: function () {
        this.items = [
            {
                itemId: 'activate',
                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                action: 'activate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'deactivate',
                text: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
                action: 'deactivate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit',
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove',
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }

});
