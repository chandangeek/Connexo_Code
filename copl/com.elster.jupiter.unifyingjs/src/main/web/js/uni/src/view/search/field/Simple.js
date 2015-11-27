Ext.define('Uni.view.search.field.Simple', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-search-criteria-simple',
    requires: [
        'Uni.view.search.field.internal.CriteriaLine'
    ],

    reset: function() {
        this.down('uni-search-internal-criterialine').reset();
        this.callParent(arguments);
    },

    onInputChange: function() {
        this.setValue(this.down('uni-search-internal-criterialine').getValue());
    },

    onInputReset: function () {
        this.setText(this.emptyText);
    },

    initComponent: function () {
        var me = this;
        this.init();

        me.callParent(arguments);
    },

    init: function () {
        var me = this;

        me.items = {
            xtype: 'uni-search-internal-criterialine',
            operator: '==',
            padding: 5,
            removable: false,
            operatorMap: {
                '==': 'uni-search-internal-input'
                //'!=': 'uni-search-internal-input'
            },
            listeners: {
                change: {
                    fn: me.onInputChange,
                    scope: me
                },
                reset: {
                    fn: me.onInputReset,
                    scope: me
                }
            }
        };
    }
});