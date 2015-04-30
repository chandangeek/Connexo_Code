/**
 * @class Uni.grid.filtertop.ComboBox
 */
Ext.define('Uni.grid.filtertop.ComboBox', {
    extend: 'Ext.form.field.ComboBox',
    xtype: 'uni-grid-filtertop-combobox',

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    emptyText: Uni.I18n.translate('grid.filter.combobox.label', 'UNI', 'Combobox'),

    queryMode: 'local',
    valueField: 'value',
    displayField: 'display',
    forceSelection: true,

    initComponent: function () {
        var me = this;

        if (Ext.isDefined(me.options) && !Ext.isDefined(me.store)) {
            me.store = me.createStoreFromOptions();
        }

        me.listConfig = me.listConfig || {};

        if (me.multiSelect) {
            Ext.apply(me.listConfig, {
                getInnerTpl: function (displayField) {
                    return '<div class="x-combo-list-item"><img src="'
                        + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" style="  top: 2px; left: -2px; position: relative;"/> {'
                        + displayField + '} </div>';
                }
            });
        }

        me.callParent(arguments);

        me.on('specialkey', function (field, event) {
            if (event.getKey() === event.ENTER) {
                me.assertValue();
                me.fireFilterUpdateEvent();
            }
        }, me);
    },

    createStoreFromOptions: function () {
        var me = this,
            options = me.options,
            store;

        store = Ext.create('Ext.data.Store', {
            fields: ['value', 'display'],
            data: options
        });

        return store;
    }
});