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
    width: 50,
    store: [
        ["==", "="],
        ["!=", "!="],
        ["BETWEEN", "Between"]
        //...
    ]
});

