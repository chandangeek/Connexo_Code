Ext.define('Est.main.view.ReadingTypesGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.reading-types-grid',
    store: 'Est.main.store.ReadingTypes',

    requires: [
        'Uni.grid.column.ReadingType'
    ],

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'readingTypes.counterText',
            count,
            'EST',
            '{0} reading types selected'
        );
    },

    columns: [
        {
            xtype: 'reading-type-column',
            dataIndex: 'readingType',
            flex: 1
        }
    ]
});