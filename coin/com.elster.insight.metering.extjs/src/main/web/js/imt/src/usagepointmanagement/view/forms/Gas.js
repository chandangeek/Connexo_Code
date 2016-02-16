Ext.define('Imt.usagepointmanagement.view.forms.Gas', {
    extend: 'Ext.form.Panel',
    alias: 'widget.gas-info-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Imt.usagepointmanagement.view.forms.fields.MeasureField',
        'Imt.usagepointmanagement.view.forms.fields.ThreeValuesField'
    ],
    defaults: {
        labelWidth: 260,
        width: 595
    },
    items: [
        {
            xtype: 'checkbox',
            name: 'grounded',
            itemId: 'up-grounded-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded')
        },
        {
            xtype: 'measurefield',
            name: 'pressure',
            itemId: 'up-pressure-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure')
        },
        {
            xtype: 'measurefield',
            name: 'physicalCapacity',
            itemId: 'up-physicalCapacity-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.physicalCapacity', 'IMT', 'Physical capacity')
        },
        {
            xtype: 'checkbox',
            name: 'limiter',
            itemId: 'up-limiter-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter'),
            listeners: {
                change: {
                    fn: function (checkbox, newValue) {
                        Ext.suspendLayouts();
                        checkbox.nextSibling('[name=loadLimiterType]').setVisible(newValue);
                        checkbox.nextSibling('[name=loadLimit]').setVisible(newValue);
                        Ext.resumeLayouts(true);
                    }
                }
            }
        },
        {
            xtype: 'textfield',
            name: 'loadLimiterType',
            itemId: 'up-loadLimiterType-textfield',
            fieldLabel: Uni.I18n.translate('general.label.loadLimiterType', 'IMT', 'Load limiter type'),
            hidden: true
        },
        {
            xtype: 'measurefield',
            name: 'loadLimit',
            itemId: 'up-loadLimit-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.loadLimit', 'IMT', 'Load limit'),
            hidden: true
        },
        {
            xtype: 'threevaluesfield',
            name: 'bypass',
            itemId: 'up-bypass-combo',
            fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass')
        },
        {
            xtype: 'combobox',
            name: 'bypassStatus',
            itemId: 'up-bypassStatus-combo',
            fieldLabel: Uni.I18n.translate('general.label.bypassStatus', 'IMT', 'Bypass status'),
            store: 'Imt.usagepointmanagement.store.BypassStatuses',
            displayField: 'displayValue',
            valueField: 'id',
            queryMode: 'local',
            forceSelection: true,
            listeners: {
                change: {
                    fn: function (combo, newValue) {
                        if (Ext.isEmpty(newValue)) {
                            combo.reset();
                        }
                    }
                }
            }
        },










        {
            xtype: 'combobox',
            name: 'phaseCode',
            itemId: 'up-phaseCode-combo',
            fieldLabel: Uni.I18n.translate('general.label.phaseCode', 'IMT', 'Phase code'),
            store: 'Imt.usagepointmanagement.store.PhaseCodes',
            displayField: 'displayValue',
            valueField: 'id',
            queryMode: 'local',
            forceSelection: true,
            listeners: {
                change: {
                    fn: function (combo, newValue) {
                        if (Ext.isEmpty(newValue)) {
                            combo.reset();
                        }
                    }
                }
            }
        },
        {
            xtype: 'measurefield',
            name: 'ratedPower',
            itemId: 'up-ratedPower-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Rated power')
        },
        {
            xtype: 'measurefield',
            name: 'estimatedLoad',
            itemId: 'up-estimatedLoad-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.estimatedLoad', 'IMT', 'Estimated load')
        },
        {
            xtype: 'measurefield',
            name: 'estimatedLoad',
            itemId: 'up-estimatedLoad-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.estimatedLoad', 'IMT', 'Estimated load')
        },
        {
            xtype: 'combobox',
            name: 'collar',
            itemId: 'up-collar-combo',
            fieldLabel: Uni.I18n.translate('general.label.collar', 'IMT', 'Collar'),
            store: 'Imt.usagepointmanagement.store.CollarValues',
            displayField: 'displayValue',
            valueField: 'value',
            queryMode: 'local',
            forceSelection: true,
            listeners: {
                change: {
                    fn: function (combo, newValue) {
                        if (Ext.isEmpty(newValue)) {
                            combo.reset();
                        }
                    }
                }
            }
        },
        {
            xtype: 'checkbox',
            name: 'interruptible',
            itemId: 'up-interruptible-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.interruptible', 'IMT', 'Interruptible')
        }
    ]
});