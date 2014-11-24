/**
 * @class Uni.grid.column.Duration
 */
Ext.define('Uni.grid.column.Duration', {
    extend: 'Ext.grid.column.Column',
    xtype: 'uni-grid-column-duration',

    requires: [
        'Uni.util.String'
    ],

    header: Uni.I18n.translate('grid.column.duration.header', 'UNI', 'Duration'),

    /**
     * @cfg usesSeconds
     *
     * If the duration field gets its value in seconds or not.
     * By default it assumes a millisecond value.
     */
    usesSeconds: false,

    renderer: function (value, metaData, record) {
        var me = metaData.column;
        if (!isNaN(value)) {
            value = me.usesSeconds ? value * 1000 : value;
            return Uni.util.String.formatDuration(parseInt(value, 10));
        }

        return value;
    }
});