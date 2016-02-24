Ext.define('Imt.usagepointmanagement.view.forms.ElectricityInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.electricity-info-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Imt.usagepointmanagement.view.forms.fields.MeasureField',
        'Imt.usagepointmanagement.view.forms.fields.ThreeValuesField',
        'Imt.usagepointmanagement.view.forms.fields.LimiterCheckbox',
        'Imt.usagepointmanagement.view.forms.fields.LoadLimiterTypeField',
        'Imt.usagepointmanagement.view.forms.fields.LoadLimitField'
    ],
    defaults: {
        labelWidth: 260,
        width: 595
    },
    items: [
        {
            itemId: 'technical-info-warning',
            xtype: 'uni-form-error-message',
            hidden: true
        },
        {
            xtype: 'checkbox',
            name: 'grounded',
            itemId: 'up-grounded-checkbox',
            fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded')
        },
        {
            xtype: 'measurefield',
            name: 'nominalServiceVoltage',
            itemId: 'up-nominalServiceVoltage-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.nominalServiceVoltage', 'IMT', 'Nominal voltage'),
            store: 'Imt.usagepointmanagement.store.measurementunits.Voltage',
            value: {value: null, unit: 'V', multiplier: 0}
        },
        {
            xtype: 'combobox',
            name: 'phaseCode',
            itemId: 'up-phaseCode-combo',
            fieldLabel: Uni.I18n.translate('general.label.phaseCode', 'IMT', 'Phase code'),
            store: 'Imt.usagepointmanagement.store.PhaseCodes',
            displayField: 'displayName',
            valueField: 'name',
            queryMode: 'local',
            forceSelection: true,
            emptyText: Uni.I18n.translate('usagepoint.add.emptyText.phaseCode', 'IMT', 'Select phase code...'),
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
            fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Rated power'),
            store: 'Imt.usagepointmanagement.store.measurementunits.Power',
            value: {value: null, unit: 'W', multiplier: 0}
        },
        {
            xtype: 'measurefield',
            name: 'estimatedLoad',
            itemId: 'up-estimatedLoad-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.estimatedLoad', 'IMT', 'Estimated load'),
            store: 'Imt.usagepointmanagement.store.measurementunits.Power',
            value: {value: null, unit: 'W', multiplier: 0}
        },
        {
            xtype: 'limitercheckbox',
            itemId: 'up-limiter-checkbox'
        },
        {
            xtype: 'loadlimitertypefield',
            itemId: 'up-loadLimiterType-textfield',
            hidden: true
        },
        {
            xtype: 'loadlimitfield',
            itemId: 'up-loadLimit-measurefield',
            store: 'Imt.usagepointmanagement.store.measurementunits.Power',
            value: {value: null, unit: 'W', multiplier: 0},
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
            itemId: 'up-interruptible-checkbox',
            fieldLabel: Uni.I18n.translate('general.label.interruptible', 'IMT', 'Interruptible')
        }
    ]
});