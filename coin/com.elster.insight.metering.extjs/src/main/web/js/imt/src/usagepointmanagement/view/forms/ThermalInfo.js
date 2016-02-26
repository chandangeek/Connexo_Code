Ext.define('Imt.usagepointmanagement.view.forms.ThermalInfo', {
    extend: 'Imt.usagepointmanagement.view.forms.BaseInfo',
    alias: 'widget.thermal-info-form',
    items: [
        {
            itemId: 'technical-info-warning',
            xtype: 'uni-form-error-message',
            hidden: true
        },
        {
            xtype: 'techinfo-measurefield',
            name: 'pressure',
            itemId: 'up-pressure-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure'),
            store: 'Imt.usagepointmanagement.store.measurementunits.PressureExtended',
            value: {value: null, unit: 'Pa', multiplier: 0}
        },
        {
            xtype: 'techinfo-measurefield',
            name: 'physicalCapacity',
            itemId: 'up-physicalCapacity-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.physicalCapacity', 'IMT', 'Physical capacity'),
            store: 'Imt.usagepointmanagement.store.measurementunits.Capacity',
            value: {value: null, unit: 'Wh', multiplier: 0}
        },
        {
            xtype: 'techinfo-bypassfield',
            itemId: 'up-bypass-combo'
        },
        {
            xtype: 'techinfo-bypassstatuscombobox',
            itemId: 'up-bypassStatus-combo',
            hidden: true
        },
        {
            xtype: 'techinfo-threevaluesfield',
            name: 'valve',
            itemId: 'up-valve-combo',
            fieldLabel: Uni.I18n.translate('general.label.valve', 'IMT', 'Valve')
        },
        {
            xtype: 'techinfo-threevaluesfield',
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