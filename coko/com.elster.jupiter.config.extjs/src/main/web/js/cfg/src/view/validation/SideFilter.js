Ext.define('Cfg.view.validation.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.cfg-side-filter',
    requires: [
        'Uni.component.filter.view.Filter'
    ],
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    ui: 'medium',
    items: [
        {
            xtype: 'nested-form',
            itemId: 'filter-form',
            ui: 'filter',
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
                    name: 'name',
                    itemId: 'readingTypeNameTextField',
                    fieldLabel: Uni.I18n.translate('validation.readingTypeName', 'CFG', 'Reading type name'),
                    displayField: 'name',
                    valueField: 'id'
                },
                {
                    itemId: 'unitsOfMeasureCombo',
                    name: 'unitOfMeasure',
                    fieldLabel: Uni.I18n.translate('validation.unitOfMeasure', 'CFG', 'Unit of measure'),
                    forceSelection: true,
                    displayField: 'name',
                    valueField: 'name'
                },
                {
                    itemId: 'timeOfUseCombo',
                    name: 'tou',
                    fieldLabel: Uni.I18n.translate('validation.timeOfUse', 'CFG', 'Time of use'),
                    forceSelection: true,
                    displayField: 'name',
                    valueField: 'tou'
                },
                {
                    itemId: 'intervalsCombo',
                    name: 'time',
                    fieldLabel: Uni.I18n.translate('validation.interval', 'CFG', 'Interval'),
                    forceSelection: true,
                    displayField: 'name',
                    valueField: 'time'
                }

            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            text: Uni.I18n.translate('validationTasks.connection.widget.sideFilter.apply', 'CFG', 'Apply'),
                            ui: 'action',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('validationTasks.connection.widget.sideFilter.clearAll', 'CFG', 'Clear all'),
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});
