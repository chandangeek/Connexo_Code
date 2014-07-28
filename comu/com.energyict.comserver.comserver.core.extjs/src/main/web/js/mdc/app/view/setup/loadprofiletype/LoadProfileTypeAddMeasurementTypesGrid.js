Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileTypeAddMeasurementTypesGrid',
    itemId: 'loadProfileTypeAddMeasurementTypesGrid',
    store: 'MeasurementTypesToAdd',
    height: 395,
    scroll: false,
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },
    selType: 'checkboxmodel',
    selModel: {
        checkOnly: true,
        enableKeyNav: false,
        showHeaderCheckbox: false
    },
    requires: [
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType'
    ],

    columns: [
        {
            header: 'Name',
            dataIndex: 'name',
            flex: 3
        },
        {
            xtype: 'obis-column',
            dataIndex: 'obisCode'
        },
        {
            xtype: 'reading-type-column',
            header: Uni.I18n.translate('registerMappings.CIMreadingType', 'MDC', 'CIM Reading type'),
            dataIndex: 'readingType',
            align: 'right'
        }
    ]
});