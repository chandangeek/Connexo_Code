/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.TimeDuration', {
    extend: 'Uni.view.search.field.Numeric',
    xtype: 'uni-search-criteria-timeduration',
    minWidth: 400,
    itemsDefaultConfig: {
        minValue: 0,
        autoStripChars: true,
        allowExponential: false
    },

    setValue: function (value) {
        if (value) {
            this.getUnitField().setValue(+value[0].get('criteria')[0].split(':')[1]);
        }

        this.callParent(arguments);
    },

    getValue: function () {
        var me = this,
            value = this.callParent(arguments);

        return value ? value.map(function (v) {
            var criteria = v.get('criteria');
            var timeUnit = me.getUnitField().getValue();
            v.set('criteria', _.map(Ext.isArray(criteria) ? criteria : [criteria], function (item) {
                return item + ':' + timeUnit
            }));
            return v;
        }) : null;
    },

    getUnitField: function () {
        return this.down('combobox[valueField=code]');
    },

    reset: function () {
        var me = this;

        me.getUnitField().reset();
        me.callParent(arguments);
    },

    initComponent: function () {
        var me = this;

        Ext.suspendLayouts();
        me.callParent(arguments);

        me.addDocked({
            xtype: 'toolbar',
            padding: '5 5 0 5',
            dock: 'top',
            items: [
                {
                    xtype: 'checkbox',
                    boxLabel: Uni.I18n.translate('general.noSpecifiedValue', 'UNI', 'No specified value'),
                    disabled: true,
                    width: 150
                },
                '->',
                {
                    xtype: 'combobox',
                    fieldLabel: Uni.I18n.translate('general.unit', 'UNI', 'Unit'),
                    store: 'Uni.property.store.TimeUnits',
                    displayField: 'localizedValue',
                    valueField: 'code',
                    forceSelection: true,
                    editable: false,
                    width: 190,
                    labelWidth: 50,
                    margin: '0 10 0 0',
                    queryMode: 'local',
                    value: 14,
                    listeners: {
                        change: {
                            fn: me.onValueChange,
                            scope: me
                        }
                    }
                }
            ]
        });

        Ext.resumeLayouts(true);
    }
});