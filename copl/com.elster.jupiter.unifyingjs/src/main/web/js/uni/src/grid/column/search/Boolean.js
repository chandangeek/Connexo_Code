/**
 * @class Uni.grid.column.search.Boolean
 */
Ext.define('Uni.grid.column.search.Boolean', {
    extend: 'Ext.grid.column.Column',
    xtype: 'uni-grid-column-search-boolean',

    renderer: function (value, metaData, record) {
        metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';

        return value ? Uni.I18n.translate('general.yes', 'UNI', 'Yes'):
            Uni.I18n.translate('general.no', 'UNI', 'No');
    }
});