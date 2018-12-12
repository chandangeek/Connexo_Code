/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.changeusagepointlifecycle.Wizard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Imt.usagepointmanagement.view.changeusagepointlifecycle.Step1',
        'Imt.usagepointmanagement.view.changeusagepointlifecycle.Step2'
    ],
    alias: 'widget.change-usage-point-life-cycle-wizard',
    layout: 'card',
    defaults: {
        ui: 'large'
    },
    router: null,
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'change-usage-point-life-cycle-step1',
                itemId: 'change-usage-point-life-cycle-step1',
                title: Ext.String.format(Uni.I18n.translate('usagePointLifeCycle.change.step.title', 'IMT', 'Step {0}:'), 1)
                + ' ' + Uni.I18n.translate('usagePointLifeCycle.select', 'IMT', 'Select usage point life cycle'),
                router: me.router
            },
            {
                xtype: 'change-usage-point-life-cycle-step2',
                router: me.router,
                itemId: 'change-usage-point-life-cycle-step2',
                title: Ext.String.format(Uni.I18n.translate('usagePointLifeCycle.change.step.title', 'IMT', 'Step {0}:'), 2)
                + ' ' + Uni.I18n.translate('general.status', 'IMT', 'Status')
            }
        ];

        me.bbar = {
            itemId: 'change-usage-point-life-cycle-buttons',
            items: [
                {
                    text: Uni.I18n.translate('general.back', 'IMT', 'Back'),
                    action: 'step-back',
                    itemId: 'change-usage-point-life-cycle-step-back',
                    disabled: true
                },
                {
                    text: Uni.I18n.translate('general.next', 'IMT', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    itemId: 'change-usage-point-life-cycle-next'
                },
                {
                    text: Uni.I18n.translate('general.finish', 'IMT', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    itemId: 'change-usage-point-life-cycle-finish',
                    hidden: true,
                    href: me.router.getQueryStringValues().previousRoute || me.router.getRoute('usagepoints/view').buildUrl(me.router.arguments)

                },
                {
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    itemId: 'change-usage-point-life-cycle-cancel',
                    href: me.router.getQueryStringValues().previousRoute || me.router.getRoute('usagepoints/view').buildUrl(me.router.arguments)
                }
            ]
        };

        me.callParent(arguments);

    }

});