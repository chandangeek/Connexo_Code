Ext.define('Uni.view.search.field.Simple', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-search-criteria-simple',
    requires: [
        'Uni.view.search.field.internal.Input'
    ],

    reset: function() {
        this.down('#filter-input').reset();
        this.callParent(arguments);
    },

    //updateButtonText: function () {
    //    Ext.isEmpty(this.value)
    //        ? this.setText(this.emptyText)
    //        : this.setText(this.emptyText + ': ' + this.value);
    //},

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'toolbar',
            layout: 'hbox',
            padding: 5,
            items: [
                {
                    itemId: 'filter-operator',
                    xtype: 'combo',
                    value: '=',
                    width: 50,
                    margin: '0 5 0 0',
                    disabled: true
                },
                {
                    xtype: 'uni-search-internal-input',
                    itemId: 'filter-input',
                    emptyText: me.emptyText,
                    listeners: {
                        change: {
                            fn: me.onChange,
                            scope: me
                        }
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});