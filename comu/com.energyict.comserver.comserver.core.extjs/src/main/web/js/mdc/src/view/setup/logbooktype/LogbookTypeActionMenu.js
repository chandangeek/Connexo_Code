/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.logbooktype.LogbookTypeActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.logbook-type-action-menu',
    itemId: 'logbook-type-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'logbookTypeActionEdit',
                action: 'editLogbookType',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'logbookTypeActionRemove',
                action: 'removeLogbookType',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
