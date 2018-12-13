/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.registers.MultipleRegisterReadingsActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.register-readings-bulk-action-menu',
    initComponent: function() {
        var me = this;

        me.items = [
            {
                itemId: 'view-history',
                privileges: Imt.privileges.UsagePoint.admin,
                text: Uni.I18n.translate('general.viewHistory', 'IMT', 'View history'),
                action: 'viewHistory',
                section: me.SECTION_ACTION
            }
        ];

        me.callParent();
    }
});
