Ext.define('Imt.usagepointmanagement.view.forms.WaterInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.water-info-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Imt.usagepointmanagement.view.forms.fields.MeasureField',
        'Imt.usagepointmanagement.view.forms.fields.ThreeValuesField',
        'Imt.usagepointmanagement.view.forms.fields.BypassField',
        'Imt.usagepointmanagement.view.forms.fields.BypassStatusCombobox',
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
            name: 'pressure',
            itemId: 'up-pressure-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure'),
            store: 'Imt.usagepointmanagement.store.measurementunits.Pressure',
            value: {value: null, unit: 'Pa', multiplier: 0}
        },
        {
            xtype: 'measurefield',
            name: 'physicalCapacity',
            itemId: 'up-physicalCapacity-checkbox',
            fieldLabel: Uni.I18n.translate('general.label.physicalCapacity', 'IMT', 'Physical capacity'),
            store: 'Imt.usagepointmanagement.store.measurementunits.Volume',
            value: {value: null, unit: 'm3/h', multiplier: 0}
        },
        {
            xtype: 'limitercheckbox',
            itemId: 'up-limiter-measurefield'
        },
        {
            xtype: 'loadlimitertypefield',
            itemId: 'up-loadLimiterType-textfield',
            hidden: true
        },
        {
            xtype: 'loadlimitfield',
            itemId: 'up-loadLimit-measurefield',
            store: 'Imt.usagepointmanagement.store.measurementunits.Volume',
            value: {value: null, unit: 'm3/h', multiplier: 0},
            hidden: true
        },
        {
            xtype: 'bypassfield',
            itemId: 'up-bypass-combo'
        },
        {
            xtype: 'bypassstatuscombobox',
            itemId: 'up-bypassStatus-combo',
            hidden: true
        },
        {
            xtype: 'threevaluesfield',
            name: 'valve',
            itemId: 'up-valve-combo',
            fieldLabel: Uni.I18n.translate('general.label.valve', 'IMT', 'Valve')
        },
        {
            xtype: 'threevaluesfield',
            name: 'collar',
            itemId: 'up-collar-combo',
            fieldLabel: Uni.I18n.translate('general.label.collar', 'IMT', 'Collar')
        },
        {
            xtype: 'threevaluesfield',
            name: 'capped',
            itemId: 'up-capped-combo',
            fieldLabel: Uni.I18n.translate('general.label.capped', 'IMT', 'Capped')
        },
        {
            xtype: 'threevaluesfield',
            name: 'clamped',
            itemId: 'up-clamped-combo',
            fieldLabel: Uni.I18n.translate('general.label.clamped', 'IMT', 'Clamped')
        }
    ]
});