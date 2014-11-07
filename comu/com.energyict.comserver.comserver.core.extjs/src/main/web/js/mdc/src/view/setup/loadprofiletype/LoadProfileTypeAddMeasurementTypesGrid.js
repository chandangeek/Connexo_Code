Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.load-profile-type-add-measurement-types-grid',
    store: 'Mdc.store.MeasurementTypesToAdd',

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
            flex: 2
        },
        {
            xtype: 'obis-column',
            dataIndex: 'obisCode',
            flex: 1
        },
        {
            xtype: 'reading-type-column',
            dataIndex: 'readingType',
            flex: 2
        }
    ]
});