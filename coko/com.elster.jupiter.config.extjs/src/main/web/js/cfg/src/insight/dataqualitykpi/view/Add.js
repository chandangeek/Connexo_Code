/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Cfg.insight.dataqualitykpi.view.fields.Purpose'
    ],
    alias: 'widget.ins-data-quality-kpi-add',
    returnLink: null,
    usagePointGroupsIsDefined: false,
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                itemId: 'ins-data-quality-kpi-add-form',
                title: me.router.getRoute().getTitle(),
                returnLink: me.returnLink,
                ui: 'large',
                defaults: {
                    labelWidth: 250,
                    width: 600
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'cmb-usage-point-group',
                        name: 'usagePointGroup',
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.uagePointGroup', 'CFG', 'Usage point group'),
                        privileges: me.usagePointGroupsIsDefined,
                        emptyText: Uni.I18n.translate('ins.dataqualitykpi.selectUsagePointGroup', 'CFG', 'Select a usage point group...'),
                        store: 'Cfg.insight.dataqualitykpi.store.UsagePointGroups',
                        queryMode: 'local',
                        forceSelection: true,
                        displayField: 'displayValue',
                        valueField: 'id'
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'no-usage-point-group-msg',
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.uagePointGroup', 'CFG', 'Usage point group'),
                        privileges: !me.usagePointGroupsIsDefined,
                        fieldStyle: 'color: #eb5642;',
                        value: Uni.I18n.translate('ins.dataqualitykpi.noUsagePointGroup', 'CFG', 'No usage point group available.')
                    },
                    {
                        xtype: 'purpose-field',
                        itemId: 'fld-purposes',
                        name: 'purposes',
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.Purpose', 'CFG', 'Purpose'),
                        hidden: true
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'cmb-frequency',
                        name: 'frequency',
                        required: true,
                        fieldLabel: Uni.I18n.translate('datavalidationkpis.calculationFrequency', 'CFG', 'Calculation frequency'),
                        emptyText: Uni.I18n.translate('datavalidationkpis.selectCalculationFrequency', 'CFG', 'Select a calculation frequency...'),
                        store: 'Cfg.store.DataValidationKpiFrequency',
                        queryMode: 'local',
                        forceSelection: true,
                        displayField: 'name',
                        valueField: 'id'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: ' ',
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                action: 'add',
                                itemId: 'add-button',
                                disabled: !me.usagePointGroupsIsDefined
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                action: 'cancel-add-button',
                                href: me.returnLink
                            }
                        ]
                    }
                ],
                listeners: {
                    afterrender: {
                        scope: me,
                        fn: me.onAfterRender
                    }
                }
            }
        ];

        me.callParent(arguments);
    },

    onAfterRender: function () {
        var me = this,
            usagePointGroupCombo = me.down('#cmb-usage-point-group');

        if (usagePointGroupCombo) {
            usagePointGroupCombo.focus();
        }
    }
});