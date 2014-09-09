Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'loadProfileTypeAddMeasurementTypesGrid',

    itemId: 'loadProfileTypeAddMeasurementTypesGrid',
    store: 'MeasurementTypesToAdd',

    requires: [
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType'
    ],

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid.counterText',
            count,
            'MDC',
            '{0} measurement types selected'
        );
    },

    allLabel: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid.allLabel', 'MDC', 'All measurement types'),
    allDescription: Uni.I18n.translate(
        'setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid.allDescription',
        'MDC',
        'Select all items (related to filters on previous screen)'
    ),

    selectedLabel: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid.selectedLabel', 'MDC', 'Selected measurement types'),
    selectedDescription: Uni.I18n.translate(
        'setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid.selectedDescription',
        'MDC',
        'Select items in table'
    ),

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
    ],

    initComponent: function () {
        var me = this;

        me.cancelHref = '#/administration/loadprofiletypes/create';
        me.callParent(arguments);
    }
});