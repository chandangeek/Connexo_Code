/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.UsagePointActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.usage-point-action-menu',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                text: Uni.I18n.translate('general.addUsagePoint.editUsagePoint', 'MDC', 'Edit'),
                href: me.router.getRoute('usagepoints/usagepoint/edit').buildUrl(),
                section: this.SECTION_EDIT
            }
        ];
        me.callParent(arguments);
    }
});


