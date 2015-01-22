Ext.define('Dxp.view.tasks.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.rt-side-filter',
    requires: [
        'Uni.component.filter.view.Filter'
    ],
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DES', 'Filter'),
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
                    fieldLabel: Uni.I18n.translate('dataExportTasks.readingTypeName', 'DES', 'Reading type name'),
                    displayField: 'name',
                    valueField: 'id'
                },
                {
                    itemId: 'unitsOfMeasureCombo',
                    name: 'unitOfMeasure',
                    fieldLabel: Uni.I18n.translate('dataExportTasks.unitOfMeasure', 'DES', 'Unit of measure'),
                    forceSelection: true,
                    displayField: 'name',
                    valueField: 'name'
                },
                {
                    itemId: 'timeOfUseCombo',
                    name: 'tou',
                    fieldLabel: Uni.I18n.translate('dataExportTasks.timeOfUse', 'DES', 'Time of use'),
                    forceSelection: true,
                    displayField: 'name',
                    valueField: 'tou'
                },
                {
                    itemId: 'intervalsCombo',
                    name: 'time',
                    fieldLabel: Uni.I18n.translate('dataExportTasks.interval', 'DES', 'Interval'),
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
                            text: Uni.I18n.translate('dataExportTasks.widget.sideFilter.apply', 'DES', 'Apply'),
                            ui: 'action',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('dataExportTasks.widget.sideFilter.clearAll', 'DES', 'Clear all'),
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});


