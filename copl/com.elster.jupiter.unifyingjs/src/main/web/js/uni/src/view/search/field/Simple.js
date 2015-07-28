Ext.define('Uni.view.search.field.Simple', {
    extend: 'Ext.button.Button',
    xtype: 'search-criteria-simple',
    menuAlign: 'tr-br',
    style: {
        'background-color': '#71adc7'
    },
    requires: [
        'Uni.view.search.field.Input'
    ],

    initComponent: function () {
        var me = this;
        me.menu = {
            plain: true,
            bodyStyle: {
                background: '#fff'
            },
            padding: 0,
            minWidth: 273,
            items: {
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
                        emptyText: me.emptyText
                    }
                ]
            }
        };

        me.callParent(arguments);
    }
});