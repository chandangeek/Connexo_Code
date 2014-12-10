Ext.define('Uni.grid.column.LastEventType', {
    extend: 'Ext.grid.column.Column',
    xtype: 'last-event-type-column',
    header: Uni.I18n.translate('lastEventType.label', 'UNI', 'Last event type'),
    minWidth: 150,
    align: 'left',

    requires: [
        'Uni.form.field.LastEventTypeDisplay'
    ],

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        var me = this;

        return new Uni.form.field.LastEventTypeDisplay().renderer.apply(me, arguments);
    }
});