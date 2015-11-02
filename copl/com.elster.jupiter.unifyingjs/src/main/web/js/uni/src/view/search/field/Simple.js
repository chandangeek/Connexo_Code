Ext.define('Uni.view.search.field.Simple', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-search-criteria-simple',
    requires: [
        'Uni.view.search.field.internal.Input',
        'Uni.view.search.field.internal.Operator'
    ],

    reset: function() {
        this.down('#filter-operator').reset();
        this.down('#filter-input').reset();
        this.callParent(arguments);
    },

    populateValue: function(value) {
        this.down('#filter-input').setValue(value);
    },

    onInputChange: function() {
        this.setValue({
            operator: this.down('#filter-operator').getValue(),
            criteria: this.down('#filter-input').getValue()
        });
    },

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'toolbar',
            layout: 'hbox',
            padding: 5,
            items: [
                {
                    itemId: 'filter-operator',
                    xtype: 'uni-search-internal-operator',
                    value: '==',
                    margin: '0 5 0 0',
                    listeners: {
                        change: {
                            fn: me.onInputChange,
                            scope: me
                        }
                    }
                },
                {
                    xtype: 'uni-search-internal-input',
                    itemId: 'filter-input',
                    emptyText: me.emptyText,
                    listeners: {
                        change: {
                            fn: me.onInputChange,
                            scope: me
                        }
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});