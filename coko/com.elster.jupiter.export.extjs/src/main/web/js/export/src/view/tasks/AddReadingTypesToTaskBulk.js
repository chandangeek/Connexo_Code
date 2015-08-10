Ext.define('Dxp.view.tasks.AddReadingTypesToTaskBulk', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.AddReadingTypesToTaskBulk',

    requires: [
        'Uni.grid.column.ReadingType'
    ],

    plugins: {
        ptype: 'bufferedrenderer'
    },

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'validation.readingTypes.counterText',
            count,
            'DES',
            '{0} reading types selected'
        );
    },

    bottomToolbarHidden: true,

    columns: [
        {
            xtype: 'reading-type-column',
            dataIndex: 'readingType',
            flex: 1
        }
    ]

});