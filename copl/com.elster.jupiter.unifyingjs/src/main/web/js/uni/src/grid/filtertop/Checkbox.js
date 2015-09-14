/**
 * @class Uni.grid.filtertop.Checkbox
 */
Ext.define('Uni.grid.filtertop.Checkbox', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-grid-filtertop-checkbox',

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    emptyText: Uni.I18n.translate('grid.filter.checkbox.label', 'UNI', 'Checkbox'),

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    groupName: undefined,
    valueField: 'value',
    displayField: 'display',
    defaultType: 'checkboxfield',

    initComponent: function () {
        var me = this;

        if (!Ext.isDefined(me.groupName)) {
            me.groupName = me.generateRandomName();
        }

        if (Ext.isDefined(me.options) && !Ext.isDefined(me.items)) {
            me.resetItemsFromOptions();
            me.items = me.createItemsFromOptions();
        }

        me.callParent(arguments);
    },

    resetItemsFromOptions: function () {
        var me = this;

        Ext.Array.each(me.options, function (option) {
            option.name = undefined;
        });
    },

    createItemsFromOptions: function () {
        var me = this,
            options = me.options,
            items = [];

        Ext.Array.each(options, function (option) {
            Ext.applyIf(option, {
                name: me.groupName,
                boxLabel: option[me.displayField],
                inputValue: option[me.valueField]
            });

            delete option[me.displayField];
            delete option[me.valueField];

            items.push(option);
        }, me);

        return items;
    },

    setFilterValue: function (data) {
        var me = this,
            values = {};

        if (Ext.isArray(data)) {
            values[me.groupName] = data;
            me.setValues(values);
        } else {
            me.setOneValue(data);
        }
    },

    setOneValue: function(value) {
      Ext.each(this.items.items, function (item) {
          if (item.inputValue === value) item.setValue(true);
      });
    },

    getParamValue: function () {
        return this.getValues()[this.groupName];
    },

    resetValue: function () {
        var me = this;

        me.items.each(function (field) {
            field.reset();
        }, me);
    }
});