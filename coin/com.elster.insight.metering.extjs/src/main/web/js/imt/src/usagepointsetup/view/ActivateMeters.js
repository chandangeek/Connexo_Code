/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointsetup.view.ActivateMeters', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagePointActivateMeters',
    router: null,
    usagePoint: null,
    meterRoles: null,
    returnLink: null,
    requires: [
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Imt.usagepointmanagement.view.forms.fields.meteractivations.MeterActivationsField'
    ],

    listeners: {
        afterrender: function () {
            this.down('#meter-activations-field').setMeterRoles(this.meterRoles);
        }
    },

    comboLimitNotification: function (combo) {
        var picker = combo.getPicker(),
            fn = function (view) {
                var store = view.getStore(),
                    el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');

                if (store.getTotalCount() > store.getCount()) {
                    el.appendChild({
                        tag: 'li',
                        html: Uni.I18n.translate('usagePoint.setMeters.keepTyping', 'IMT', 'Keep typing to narrow down'),
                        cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                    });
                }
            };

        picker.on('refresh', fn);
        picker.on('beforehide', function () {
            picker.un('refresh', fn);
        }, combo, {single: true});
    },

    initComponent: function () {
        var me = this;

        me.content = [{
            xtype: 'panel',
            itemId: 'usage-point-edit-meters',
            title: Uni.I18n.translate('general.linkMeters', 'IMT', 'Link meters'),
            ui: 'large',
            layout: {},
            items: [
                {
                    xtype: 'meter-activations-field',
                    itemId: 'meter-activations-field',
                    name: 'metrologyConfiguration.meterRoles',
                    meterRoles: true,
                    listeners: {
                        meterActivationsChange: function (allMetersSpecified) {
                            me.fireEvent('meterActivationsChange', allMetersSpecified);
                        }
                    }
                }
            ],
            bbar: {
                items: [{
                    xtype: 'button',
                    itemId: 'save-btn',
                    ui: 'action',
                    text: Uni.I18n.translate('general.save', 'IMT', 'Save'),
                    usagePoint: me.usagePoint,
                    meterRoles: me.meterRoles
                }, {
                    xtype: 'button',
                    ui: 'link',
                    href: me.returnLink,
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel')
                }]
            }
        }];
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint
                    }
                ]
            }
        ];

        me.content[0].items.push({
            xtype: 'label',
            title: 'nusddd',
            hidden: true,
            cls: 'x-form-invalid-under',
            itemId: 'stageErrorLabel',
            margin: '0 0 0 135' // labelWidth (120) + 15
        });
        me.callParent(arguments);

    }
});