/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.protocol-dialect-action-menu',
    itemId: 'protocol-dialect-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'editProtocolDialect',
                action: 'editProtocolDialect',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    }
});
