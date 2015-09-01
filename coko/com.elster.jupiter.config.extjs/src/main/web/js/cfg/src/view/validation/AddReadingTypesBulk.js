Ext.define('Cfg.view.validation.AddReadingTypesBulk', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.addReadingTypesBulk',
    store: 'Cfg.store.ReadingTypesToAddForRule',

    requires: [
        'Uni.grid.column.ReadingType'
    ],

    plugins: {
        ptype: 'bufferedrenderer'
    },

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfReadingTypes.selected', count, 'CFG',
            'No reading types selected', '{0} reading type selected', '{0} reading types selected'
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