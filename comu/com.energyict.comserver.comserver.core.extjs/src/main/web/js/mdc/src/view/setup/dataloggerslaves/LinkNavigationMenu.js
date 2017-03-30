/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.LinkNavigationMenu', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.dataloggerslave-link-navigation',
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    padding: '0 0 0 0',
    title: Uni.I18n.translate('general.linkDataLoggerSlave', 'MDC', 'Link data logger slave'),

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'mdc-select-slave',
                text: Uni.I18n.translate('general.selectDataLoggerSlave', 'MDC', 'Select data logger slave')
            },
            {
                itemId: 'mdc-map-channels',
                text: Uni.I18n.translate('general.mapChannels', 'MDC', 'Map channels')
            },
            {
                itemId: 'mdc-map-registers',
                text: Uni.I18n.translate('general.mapRegisters', 'MDC', 'Map registers')
            },
            {
                itemId: 'mdc-confirm-arrival-date',
                text: Uni.I18n.translate('general.selectLinkingDate', 'MDC', 'Select linking date')
            },
            {
                itemId: 'mdc-confirmation',
                text: Uni.I18n.translate('general.confirmation', 'MDC', 'Confirmation')
            },
            {
                itemId: 'mdc-status',
                text: Uni.I18n.translate('general.status', 'MDC', 'Status')
            }
        ];

        me.callParent(arguments);
    }
});
