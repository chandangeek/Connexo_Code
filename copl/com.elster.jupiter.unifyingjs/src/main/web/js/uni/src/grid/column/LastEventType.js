Ext.define('Uni.grid.column.LastEventType', {
    extend: 'Ext.grid.column.Column',
    xtype: 'last-event-type-column',
    header: Uni.I18n.translate('lastEventType.label', 'UNI', 'Last event type'),
    minWidth: 150,
    align: 'left',

    requires: [
        'Uni.form.field.LastEventTypeDisplay'
    ],

    deferredRenderer: function (value, record, view) {
        var me = this;
        var cmp = view.getCell(record, me).down('.x-grid-cell-inner');
        var field = new Uni.form.field.LastEventTypeDisplay({
            fieldLabel: false
        });
        cmp.setHTML('');
        field.setValue(value);
        field.render(cmp);

        Ext.defer(view.updateLayout, 10, view);
    },

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = metaData.column;
        Ext.defer(me.deferredRenderer, 1, me, [value, record, view]);
    }
});