Ext.define('Imt.usagepointmanagement.view.forms.ElectricityInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.electricity-info-form',
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
            name: 'nominalServiceVoltage',
            itemId: 'up-nominalServiceVoltage-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.nominalServiceVoltage', 'IMT', 'Nominal voltage')
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
                    fn: function (field, newValue) {
                        if (Ext.isEmpty(newValue)) {
                            field.reset();
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
            xtype: 'checkbox',
            name: 'limiter',
            itemId: 'up-limiter-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter'),
            listeners: {
                change: {
                    fn: function (field, newValue) {
                        if (field.rendered) {
                            Ext.suspendLayouts();
                            field.nextSibling('[name=loadLimiterType]').setVisible(newValue);
                            field.nextSibling('[name=loadLimit]').setVisible(newValue);
                            Ext.resumeLayouts(true);
                        }
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
            name: 'collar',
            itemId: 'up-collar-combo',
            fieldLabel: Uni.I18n.translate('general.label.collar', 'IMT', 'Collar')
        },
        {
            xtype: 'checkbox',
            name: 'interruptible',
            itemId: 'up-interruptible-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.interruptible', 'IMT', 'Interruptible')
        }
    ]
});