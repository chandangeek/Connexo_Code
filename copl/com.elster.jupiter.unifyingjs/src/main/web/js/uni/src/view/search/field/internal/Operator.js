/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.search.field.internal.Adapter
 */
Ext.define('Uni.view.search.field.internal.Operator', {
    extend: 'Ext.form.field.ComboBox',
    xtype: 'uni-search-internal-operator',
    allowBlank: false,
    forceSelection: true,
    editable: false,
    queryMode: 'local',
    width: 90,
    store: [
        ["==", "="],
        ["!=", "!="],
        [">", ">"],
        [">=", ">="],
        ["<", "<"],
        ["<=", "<="],
        ["BETWEEN", Uni.I18n.translate('search.field.internal.between', 'UNI', 'Between')],
        ["NOTNULL", Uni.I18n.translate('search.field.internal.notEmpty', 'UNI', 'Not empty')]
        //...
    ],
    config: {
        operators: null
    },

    constructor: function(config) {
        var me = this;
        if (config.operators) {
            me.store = _.filter(me.store, function(i){return config.operators.indexOf(i[0]) >= 0});
        }

        me.callParent(arguments);
        me.originalValue = me.operator;
    }
});

