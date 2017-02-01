/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.changedevicelifecycle.Wizard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mdc.view.setup.devicetype.changedevicelifecycle.Step1',
        'Mdc.view.setup.devicetype.changedevicelifecycle.Step2'
    ],
    alias: 'widget.change-device-life-cycle-wizard',
    layout: 'card',
    defaults: {
        ui: 'large'
    },
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'change-device-life-cycle-step1',
                itemId: 'change-device-life-cycle-step1',
                title: Ext.String.format(Uni.I18n.translate('deviceLifeCycle.change.step.title', 'MDC', 'Step {0}:'), 1)
                + ' ' + Uni.I18n.translate('deviceLifeCycle.select', 'MDC', 'Select device life cycle'),
                router: me.router
            },
            {
                xtype: 'change-device-life-cycle-step2',
                router: me.router,
                itemId: 'change-device-life-cycle-step2',
                title: Ext.String.format(Uni.I18n.translate('deviceLifeCycle.change.step.title', 'MDC', 'Step {0}:'), 2)
                + ' ' + Uni.I18n.translate('general.status', 'MDC', 'Status')
            }
        ];

        me.bbar = {
            itemId: 'change-device-life-cycle-buttons',
            items: [
                {
                    text: Uni.I18n.translate('general.back', 'MDC', 'Back'),
                    action: 'step-back',
                    itemId: 'change-device-life-cycle-step-back',
                    disabled: true
                },
                {
                    text: Uni.I18n.translate('general.next', 'MDC', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    itemId: 'change-device-life-cycle-next'
                },
                {
                    text: Uni.I18n.translate('general.finish', 'MDC', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    itemId: 'change-device-life-cycle-finish',
                    hidden: true,
                    href: me.router.getQueryStringValues().previousRoute || me.router.getRoute('administration/devicetypes').buildUrl()
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    itemId: 'change-device-life-cycle-cancel',
                    href: me.router.getQueryStringValues().previousRoute || me.router.getRoute('administration/devicetypes').buildUrl()
                }
            ]
        };

        me.callParent(arguments);
    }
});