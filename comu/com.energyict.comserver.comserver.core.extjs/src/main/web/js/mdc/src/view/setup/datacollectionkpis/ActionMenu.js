/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.datacollectionkpis.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.dataCollectionKpisActionMenu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
