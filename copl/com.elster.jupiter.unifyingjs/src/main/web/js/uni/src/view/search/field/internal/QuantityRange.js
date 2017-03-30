/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.QuantityRange', {
    extend: 'Ext.form.FieldSet',
    alias: 'widget.uni-search-internal-quantityrange',
    requires: [
        'Uni.view.search.field.internal.NumberField'
    ],
    width: '455',
    layout: 'hbox',
    defaults: {
        margin: '0 0 5 0'
    },
    margin: 0,
    padding: 0,
    border: false,
    unitsStore: null,

    setValue: function(value) {
        var me = this,
            valueRegExp = /(\d*)\:\d*\:.*/,
            unitRegExp = /\d*(\:\d*\:.*)/;

        if (Ext.isArray(value)) {
            Ext.suspendLayouts();
            if (!Ext.isEmpty(value[0])) {
                me.down('#from').setValue(value[0].replace(valueRegExp, '$1'));
                me.down('#unit-combo').setValue(value[0].replace(unitRegExp, '0$1'));
            }
            if (!Ext.isEmpty(value[1])) {
                me.down('#to').setValue(value[1].replace(valueRegExp, '$1'));
            }
            Ext.resumeLayouts(true);
        }
    },

    onChange: function () {
        this.fireEvent('change', this, this.getValue());
    },

    getValue: function () {
        var me = this,
            value = [],
            fromValue = me.down('#from').getValue(),
            toValue = me.down('#to').getValue(),
            unitFieldValue = me.down('#unit-combo').getValue(),
            unitRegExp = /\d*(\:\d*\:.*)/;

        if (!Ext.isEmpty(fromValue)) {
            value.push(unitFieldValue.replace(unitRegExp, fromValue + '$1'));
        }
        if (!Ext.isEmpty(toValue)) {
            value.push(unitFieldValue.replace(unitRegExp, toValue + '$1'));
        }

        return Ext.isEmpty(value) ? null : value;
    },

    reset: function() {
        Ext.suspendLayouts();
        this.items.each(function(item){
            if (item.xtype === 'uni-search-internal-numberfield' || item.xtype === 'combobox') {
                item.reset();
            }
        });
        Ext.resumeLayouts(true);
    },

    initComponent: function () {
        var me = this,
            listeners = {
                change: {
                    fn: me.onChange,
                    scope: me
                }
            },
            store = me.unitsStore,
            defaultUnit = store ? store.getAt(0) : null;

        me.addEvents(
            "change"
        );

        me.items = [
            {
                xtype: 'uni-search-internal-numberfield',
                itemId: 'from',
                listeners: listeners,
                itemsDefaultConfig: me.itemsDefaultConfig,
                minValue: 0,
                autoStripChars: true,
                allowExponential: false,
                width: 90
            },
            {
                xtype: 'label',
                itemId: 'label',
                text: Uni.I18n.translate('general.and', 'UNI', 'And').toLowerCase(),
                padding: 5,
                margin: 0
            },
            {
                xtype: 'uni-search-internal-numberfield',
                itemId: 'to',
                listeners: listeners,
                itemsDefaultConfig: me.itemsDefaultConfig,
                minValue: 0,
                autoStripChars: true,
                allowExponential: false,
                width: 90
            },
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