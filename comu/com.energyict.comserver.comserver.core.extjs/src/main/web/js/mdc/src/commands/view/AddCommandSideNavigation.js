/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.AddCommandSideNavigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.add-command-side-navigation',
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    padding: '0 0 0 0',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'mdc-add-command-side-navigation-item1',
                text: Uni.I18n.translate('general.selectDeviceGroup', 'MDC', 'Select device group')
            },
            {
                itemId: 'mdc-add-command-side-navigation-item2',
                text: Uni.I18n.translate('general.selectCommand', 'MDC', 'Select command')
            },
            {
                itemId: 'mdc-add-command-side-navigation-item3',
                text: Uni.I18n.translate('general.confirmation', 'MDC', 'Confirmation')
            },
            {
                itemId: 'mdc-add-command-side-navigation-item4',
                text: Uni.I18n.translate('general.status', 'MDC', 'Status')
            }
        ];
        me.callParent(arguments);
    }
});
