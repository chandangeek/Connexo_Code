/**
 * @class Uni.form.field.ObisDisplay
 */
Ext.define('Uni.form.field.ObisDisplay', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.obis-displayfield',
    itemId: 'obis-displayfield',
    name: 'obisCode',
    cls: 'obisCode',
    fieldLabel: Uni.I18n.translate('obis.label', 'UNI', 'OBIS code'),
    emptyText: '-'
});