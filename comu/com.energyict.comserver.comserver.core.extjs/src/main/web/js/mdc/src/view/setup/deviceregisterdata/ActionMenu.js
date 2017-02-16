/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceregisterdataactionmenu',
    itemId: 'deviceregisterdataactionmenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'confirm-value',
                hidden: true,
                text: Uni.I18n.translate('general.confirm', 'MDC', 'Confirm'),
                action: 'confirmValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'editData',
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editData',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'removeData',
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'removeData',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
