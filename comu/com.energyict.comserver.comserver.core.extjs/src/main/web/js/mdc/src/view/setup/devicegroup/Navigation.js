/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.devicegroup-add-navigation',
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    padding: '0 0 0 0',
    isEdit: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'General',
                text: me.isEdit
                    ? Uni.I18n.translate('general.setGroupName', 'MDC', 'Set group name')
                    : Uni.I18n.translate('general.generalAttributes', 'MDC', 'General attributes')
            },
            {
                itemId: 'DeviceGroup',
                text: Uni.I18n.translate('general.selectDevices', 'MDC', 'Select devices')
            },
            {
                itemId: 'Confirmation',
                text: Uni.I18n.translate('general.confirmation', 'MDC', 'Confirmation')
            },
            {
                itemId: 'Status',
                text: Uni.I18n.translate('general.status', 'MDC', 'Status')
            }
        ];

        me.callParent(arguments);
    }
});
