Ext.define('Uni.view.search.field.Simple', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'search-criteria-simple',
    menuAlign: 'tr-br',
    requires: [
        'Uni.view.search.field.internal.Input'
    ],

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
                    xtype: 'search-criteria-input',
                    emptyText: me.emptyText,
                    listeners: {
                        change: {
                            fn: me.onChange,
                            scope: me
                        },
                        reset: {
                            fn: me.reset,
                            scope: me
                        }
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});