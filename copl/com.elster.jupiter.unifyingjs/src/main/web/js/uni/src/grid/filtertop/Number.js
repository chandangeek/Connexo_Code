/**
 * @class Uni.grid.filtertop.Number
 */
Ext.define('Uni.grid.filtertop.Number', {
    extend: 'Ext.form.field.Number',
    xtype: 'uni-grid-filtertop-number',

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    emptyText: Uni.I18n.translate('grid.filter.text.label', 'UNI', 'Number'),

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        me.on('specialkey', function (field, event) {
            if (event.getKey() === event.ENTER) {
                me.fireFilterUpdateEvent();
            }
        }, me);
    }
});