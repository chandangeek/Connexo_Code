/**
 * @class Uni.grid.column.Obis
 */
Ext.define('Uni.grid.column.Obis', {
    extend: 'Ext.grid.column.Column',
    alias: 'widget.obis-column',
    itemId: 'obis-column',
    header: Uni.I18n.translate('obis.label', 'UNI', 'OBIS code'),
    width: 140,
    align: 'left'
});