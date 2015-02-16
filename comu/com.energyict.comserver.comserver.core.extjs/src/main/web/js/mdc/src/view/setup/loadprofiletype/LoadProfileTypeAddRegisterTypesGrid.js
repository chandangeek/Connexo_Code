Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.load-profile-type-add-register-types-grid',
    store: 'Mdc.store.RegisterTypesToAdd',

    requires: [
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType'
    ],

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid.counterText',
            count,
            'MDC',
            '{0} register types selected'
        );
    },

    allLabel: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid.allLabel', 'MDC', 'All register types'),
    allDescription: Uni.I18n.translate(
        'setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid.allDescription',
        'MDC',
        'Select all items (related to filters on previous screen)'
    ),

    selectedLabel: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid.selectedLabel', 'MDC', 'Selected register types'),
    selectedDescription: Uni.I18n.translate(
        'setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid.selectedDescription',
        'MDC',
        'Select items in table'
    ),

    columns: [
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