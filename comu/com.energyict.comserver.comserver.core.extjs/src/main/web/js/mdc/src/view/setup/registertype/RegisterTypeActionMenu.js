/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.registertype.RegisterTypeActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.register-type-action-menu',
    itemId: 'register-type-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'editRegisterType',
                action: 'editRegisterType',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'deleteRegisterType',
                action: 'deleteRegisterType',
                section: this.SECTION_REMOVE

            }
        ];
        this.callParent(arguments);
    }
});
