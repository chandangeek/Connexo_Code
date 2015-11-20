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
    width: 75,
    store: [
        ["==", "="],
        ["!=", "!="],
        [">", ">"],
        [">=", ">="],
        ["<", "<"],
        ["<=", "<="],
        ["BETWEEN", "Between"],
        ["NOTNULL", "Not empty"]
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

