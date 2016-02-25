Ext.define('Imt.usagepointmanagement.view.forms.GasInfo', {
    extend: 'Imt.usagepointmanagement.view.forms.BaseInfo',
    alias: 'widget.gas-info-form',
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
            itemId: 'up-physicalCapacity-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.physicalCapacity', 'IMT', 'Physical capacity'),
            store: 'Imt.usagepointmanagement.store.measurementunits.Volume',
            value: {value: null, unit: 'm3/h', multiplier: 0}
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
        },
        {
            xtype: 'checkbox',
            name: 'interruptible',
            itemId: 'up-interruptible-checkbox',
            fieldLabel: Uni.I18n.translate('general.label.interruptible', 'IMT', 'Interruptible')
        }
    ]
});