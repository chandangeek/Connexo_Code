Ext.define('Est.main.view.ReadingTypesSideFilter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.reading-types-side-filter',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        xtype: 'combobox',
        labelAlign: 'top',
        labelPad: 0
    },
    items: [
        {
            xtype: 'textfield',
            itemId: 'name-field',
            name: 'name',
            fieldLabel: Uni.I18n.translate('general.readingTypeName', 'EST', 'Reading type name')
        },
        {
            itemId: 'unit-of-measure-field',
            name: 'unitOfMeasure',
            fieldLabel: Uni.I18n.translate('general.unitOfMeasure', 'EST', 'Unit of measure'),
            store: 'Est.main.store.UnitsOfMeasure',
            displayField: 'name',
            valueField: 'name',
            forceSelection: true
        },
        {
            itemId: 'time-of-use-field',
            name: 'tou',
            fieldLabel: Uni.I18n.translate('general.timeOfUse', 'EST', 'Time of use'),
            store: 'Est.main.store.TimeOfUse',
            displayField: 'name',
            valueField: 'tou',
            forceSelection: true
        },
        {
            itemId: 'interval-of-use-field',
            name: 'time',
            fieldLabel: Uni.I18n.translate('general.interval', 'EST', 'Interval'),
            store: 'Est.main.store.Intervals',
            displayField: 'name',
            valueField: 'time',
            forceSelection: true
        }
    ],
    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [
                {
                    itemId: 'apply-filter',
                    text: Uni.I18n.translate('general.apply', 'EST', 'Apply'),
                    ui: 'action',
                    action: 'applyFilter'
                },
                {
                    itemId: 'clear-filter',
                    text: Uni.I18n.translate('general.clearAll', 'EST', 'Clear all'),
                    action: 'clearFilter'
                }
            ]
        }
    ]
});