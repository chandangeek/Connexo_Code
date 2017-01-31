/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.view.relativeperiod.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.relative-periods-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'view-details',
                text: Uni.I18n.translate('general.viewDetails', 'TME', 'View details'),
                action: 'viewDetails',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'remove-period',
                text: Uni.I18n.translate('general.remove', 'TME', 'Remove'),
                privileges : Tme.privileges.Period.admin,
                action: 'removePeriod',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});

