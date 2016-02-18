Ext.define('Imt.usagepointmanagement.view.forms.ThermalInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.thermal-info-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Imt.usagepointmanagement.view.forms.fields.MeasureField',
        'Imt.usagepointmanagement.view.forms.fields.ThreeValuesField',
        'Imt.usagepointmanagement.view.forms.fields.BypassField',
        'Imt.usagepointmanagement.view.forms.fields.BypassStatusCombobox'
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
            xtype: 'measurefield',
            name: 'pressure',
            itemId: 'up-pressure-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure'),
            store: 'Imt.usagepointmanagement.store.measurementunits.PressureExtended',
            value: {value: null, unit: 'Pa', multiplier: 0}
        },
        {
            xtype: 'measurefield',
            name: 'physicalCapacity',
            itemId: 'up-physicalCapacity-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.physicalCapacity', 'IMT', 'Physical capacity'),
            store: 'Imt.usagepointmanagement.store.measurementunits.Capacity',
            value: {value: null, unit: 'Wh', multiplier: 0}
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
            xtype: 'checkbox',
            name: 'interruptible',
            itemId: 'up-interruptible-checkbox',
            fieldLabel: Uni.I18n.translate('general.label.interruptible', 'IMT', 'Interruptible')
        }
    ]
});