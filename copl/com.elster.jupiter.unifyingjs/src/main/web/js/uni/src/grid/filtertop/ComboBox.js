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
    margins: '0 16 0 0',

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

    setFilterValue: function (data) {
        var me = this;
           if (Ext.isArray(data) && me.isArrayOfNumbers(data)) {
               for (var i = 0; i < data.length; i++) {
                   data[i] = parseInt(data[i]);
               }
           }

        Ext.isNumeric(data) ? me.setValue(parseInt(data)) : me.setValue(data);
    },

    isArrayOfNumbers: function (array) {
        for (var i = 0; i < array.length; i++) {
            if (!Ext.isNumeric(array[i])) {
                return false;
            }
        }

        return true;
    },

    getParamValue: function () {
        var me = this,
            value = me.getValue() || undefined;

        if (me.multiSelect && Ext.isDefined(value) && !Ext.isArray(value)) {
            value = [value];
        }

        return value;
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