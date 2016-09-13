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
    highlightedDisplayValue: null,
    highlightedValue: null,

    listeners: {
        change: {
            fn: function () {
                if (this.getValue() === null) {
                    this.reset();
                }
            }
        }
    },

    initComponent: function () {
        var me = this;

        if (Ext.isDefined(me.options) && !Ext.isDefined(me.store)) {
            me.store = me.createStoreFromOptions();
        } else {
            me.store = Ext.getStore(me.store) || Ext.create(me.store);
        }

        me.listConfig = me.listConfig || {};

        if (me.multiSelect) {
            Ext.apply(me.listConfig, {
                getInnerTpl: function (displayField) {
                    return '<div class="x-combo-list-item"><img src="'
                        + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" style="  top: 2px; left: -2px; position: relative;"/> {'
                        + displayField + ':htmlEncode} </div>';
                }
            });
        }

        me.callParent(arguments);
        me.on('specialkey', function (field, event) {
            if (event.getKey() === event.ENTER) {
                if (me.highlightedValue !== null && !_.contains(me.getFilterValue(), me.highlightedValue)) {
                    var prevValue = me.getFilterValue();
                    if (prevValue && Ext.isArray(prevValue)) {
                        prevValue.push(me.highlightedValue);
                        me.setFilterValue(prevValue);
                    } else {
                        me.setFilterValue(me.highlightedValue);
                    }
                }
                me.assertValue();
                me.fireFilterUpdateEvent();
            } else if (event.getKey() === event.ESC) {
                me.highlightedDisplayValue = null;
                me.highlightedValue = null;
            }
        }, me);
        me.getPicker().on('highlightitem', function(view, node, eOpts) {
            me.highlightedDisplayValue = node.innerText;
            me.highlightedValue = me.getValueForDisplayValue(node.innerText);
        }, me);
        me.getPicker().on('unhighlightitem', function(view, node, eOpts) {
            if (me.highlightedDisplayValue === node.innerText) {
                me.highlightedDisplayValue = null;
                me.highlightedValue = null;
            }
        }, me);

    },

    setFilterValue: function (data) {
        var me = this;
        if (Ext.isArray(data)) {
            if (me.isArrayOfNumerics(data)) {
                for (var i = 0; i < data.length; i++) {
                    data[i] = parseInt(data[i]);
                }
            }
            me.setValue(data);
        } else {
            Ext.isNumeric(data) ? me.setValue(parseInt(data)) : me.setValue(data);
        }
    },

    isArrayOfNumerics: function (array) {
        for (var i = 0; i < array.length; i++) {
            if (!Ext.isNumeric(array[i])) {
                return false;
            }
        }

        return true;
    },

    getParamValue: function () {
        var me = this,
            value = me.getValue()!== null?me.getValue():undefined;

        if (me.multiSelect && Ext.isDefined(value) && !Ext.isArray(value)) {
            value = [value];
        }

        return value;
    },

    getParamDisplayValue: function () {
        var me = this,
            value = me.getValue()!== null?me.getValue():undefined,
            valueField = me.valueField,
            displayValue = [];

        if (!Ext.isArray(value)){
            value = [value];
        }
        if (value.length > 0){
            me.getStore().each(function(rec){
                if(value.indexOf(rec.data[valueField])!=-1){
                    displayValue.push(rec.get(me.displayField));
                    return true;
                }
            });
        }

        if (me.multiSelect && Ext.isDefined(displayValue) && !Ext.isArray(displayValue)) {
            displayValue = [displayValue];
        }

        return displayValue;
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
    },

    getValueForDisplayValue: function(displayValue) {
        var me = this,
            result = null;

        Ext.Array.each(me.store.data.items, function(arrayItem) {
            if (arrayItem.get(me.displayField) === displayValue) {
                result = arrayItem.get(me.valueField);
                return false; // stop iterating
            }
        });
        return result;
    }
});