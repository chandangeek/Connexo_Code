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
        'Imt.usagepointmanagement.view.UsagePointSideMenu'
    ],

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
                    xtype: 'form',
                    itemId: 'edit-form',
                    defaults: {
                        xtype: 'combobox',
                        labelWidth: 120,
                        width: 360,
                        multiSelect: false,
                        emptyText: Uni.I18n.translate('usagepoint.setMeters.strtTyping', 'IMT', 'Start typing to select a meter'),
                        store: 'Imt.usagepointsetup.store.Devices',
                        displayField: 'name',
                        valueField: 'name',
                        anyMatch: true,
                        queryMode: 'remote',
                        queryParam: 'like',
                        queryCaching: false,
                        minChars: 1,
                        loadStore: false,
                        forceSelection: false,
                        listeners: {
                            expand: {
                                fn: me.comboLimitNotification
                            },
                            change: {
                                fn: function (combo, newValue) {
                                    var index = combo.getStore().findExact('name', newValue);
                                    if (index >= 0) {
                                        combo.meterData = combo.getStore().getAt(index).getData();
                                    } else {
                                        combo.meterData = null
                                    }
                                }
                            },
                            blur: {
                                fn: function (combo) {
                                    Ext.isEmpty(combo.meterData) && combo.setValue('');
                                }
                            }
                        }
                    },
                    getMeterActivations: function () {
                        var meterActivations = [];
                        Ext.each(this.getForm().getFields().items, function (combo) {
                            meterActivations.push(
                                {
                                    meter: {
                                        name: combo.getValue()
                                    },
                                    meterRole: {
                                        id: combo.name,
                                        name: combo.fieldLabel
                                    }
                                }
                            );
                        });
                        return meterActivations
                    },
                    items: me.meterRoles,
                    bbar: {
                        margin: '0 0 0 134',
                        items: [{
                            xtype: 'button',
                            itemId: 'save-btn',
                            ui: 'action',
                            text: Uni.I18n.translate('general.save', 'IMT', 'Save'),
                            usagePoint: this.usagePoint
                        }, {
                            xtype: 'button',
                            ui: 'link',
                            href: me.returnLink,
                            text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel')
                        }]
                    }
                }
            ]
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
        me.callParent(arguments);

    }
});