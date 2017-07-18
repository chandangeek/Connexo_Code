/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.QuantityField', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-search-internal-quantityfield',
    width: '455',
    layout: 'hbox',
    unitsStore: null,
    itemsDefaultConfig: {},

    setValue: function(value) {
        var me = this,
            valueRegExp = /(\d*)\:\d*\:.*/,
            unitRegExp = /\d*(\:\d*\:.*)/;

        if (!Ext.isEmpty(value)) {
            Ext.suspendLayouts();
            me.down('#filter-input').setValue(value.replace(valueRegExp, '$1'));
            me.down('#unit-combo').setValue(value.replace(unitRegExp, '0$1'));
            Ext.resumeLayouts(true);
        }
    },

    getValue: function() {
        var me = this,
            unitRegExp = /\d*(\:\d*\:.*)/,
            value = me.down('#filter-input').getValue();

        return !Ext.isEmpty(value) ? me.down('#unit-combo').getValue().replace(unitRegExp, value + '$1') : null;
    },

    reset: function() {
        var me = this;

        Ext.suspendLayouts();
        me.down('#filter-input').reset();
        me.down('#unit-combo').reset();
        Ext.resumeLayouts(true);

        me.fireEvent('reset', this);
    },

    onChange: function() {
        var me = this;

        me.fireEvent('change', me, me.getValue());
    },

    initComponent: function () {
        var me = this,
            store = me.unitsStore,
            defaultUnit = store ? store.getAt(0) : null;

        me.addEvents(
            "change",
            "reset"
        );

        me.items = [
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'filter-input',
                width: 180,
                margin: '0 5 0 0',
                minValue: 0,
                autoStripChars: true,
                allowExponential: false,
                listeners: {
                    change:{
                        fn: me.onChange,
                        scope: me
                    }
                }
            }, me.itemsDefaultConfig),
            {
                xtype: 'combobox',
                itemId: 'unit-combo',
                fieldLabel: undefined,
                store: store,
                valueField: 'id',
                displayField: 'displayValue',
                forceSelection: true,
                editable: false,
                width: 70,
                labelWidth: 0,
                queryMode: 'local',
                value: defaultUnit.getId() || null,
                listeners: {
                    change: {
                        fn: me.onChange,
                        scope: me
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});