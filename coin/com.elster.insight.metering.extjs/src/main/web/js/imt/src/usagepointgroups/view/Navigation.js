/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.usagepointgroup-add-navigation',
    xtype: 'usagepointgroup-add-navigation',
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    padding: '0 0 0 0',
    isEdit: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'usagepointgroup-general',
                text: me.isEdit
                    ? Uni.I18n.translate('general.setGroupName', 'IMT', 'Set group name')
                    : Uni.I18n.translate('general.generalAttributes', 'IMT', 'General attributes')
            },
            {
                itemId: 'usagepointgroup-select',
                text: Uni.I18n.translate('general.selectUsagePoints', 'IMT', 'Select usage points')
            },
            {
                itemId: 'usagepointgroup-confirmation',
                text: Uni.I18n.translate('general.confirmation', 'IMT', 'Confirmation')
            },
            {
                itemId: 'usagepointgroup-status',
                text: Uni.I18n.translate('general.status', 'IMT', 'Status')
            }
        ];

        me.callParent(arguments);
    }
});
