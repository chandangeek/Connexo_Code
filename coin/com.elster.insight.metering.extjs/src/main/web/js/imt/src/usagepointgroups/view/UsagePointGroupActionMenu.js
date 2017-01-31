/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.UsagePointGroupActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.usagepointgroup-action-menu',
    xtype: 'usagepointgroup-action-menu',
    record: null,

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                itemId: 'edit-usagepointgroup',
                action: 'editUsagePointGroup',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.usagePointGroup.remove', 'IMT', 'Remove'),
                itemId: 'remove-usagepointgroup',
                action: 'removeUsagePointGroup',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
