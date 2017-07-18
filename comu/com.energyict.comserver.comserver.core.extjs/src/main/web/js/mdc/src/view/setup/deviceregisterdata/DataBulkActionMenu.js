/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.DataBulkActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.register-data-bulk-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'view-history',
                text: Uni.I18n.translate('general.viewHistory', 'MDC', 'View history'),
                action: 'viewHistory',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});
