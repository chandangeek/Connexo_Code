Ext.define('Imt.usagepointmanagement.view.forms.ThermalInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.thermal-info-form',
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
            xtype: 'threevaluesfield',
            name: 'bypass',
            itemId: 'up-bypass-combo',
            fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass'),
            listeners: {
                change: {
                    fn: function (field, newValue) {
                        if (field.rendered) {
                            field.nextSibling('[name=bypassStatus]').setVisible(newValue);
                        }
                    }
                }
            }
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
            hidden: true,
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
            itemId: 'up-interruptible-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.interruptible', 'IMT', 'Interruptible')
        }
    ]
});