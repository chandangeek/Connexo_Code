Ext.define('Uni.view.search.field.internal.Input', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-input',
    layout: 'fit',
    flex: 1,
    padding: 0,
    style: {
        border: '1px solid #a0a0a0',
        borderRadius: '5px'
    },

    onChange: function(elm, value) {
        this.down('#filter-clear').setVisible(!!value);
        this.fireEvent('change', this, value);
    },

    reset: function() {
        this.down('#filter-input').reset();
        this.fireEvent('reset');
    },

    initComponent: function () {
        var me = this;

        me.items = {
            itemId: 'filter-input',
            xtype: 'textfield',
            flex: 1,
            emptyText: me.emptyText,
            fieldStyle: {
            border: 0,
                margin: 0
            },
            listeners: {
                change: {
                    fn: me.onChange,
                    scope: me
                }
            }
        };

        me.addEvents(
            "change",
            "reset"
        );

        me.rbar = {
            xtype: 'button',
            itemId: 'filter-clear',
            hidden: true,
            ui: 'plain',
            tooltip: 'Clear filter',
            iconCls: ' icon-close4',
            padding: 6,
            margin: 0,
            style: {
                fontSize: '16px'
            },
            listeners: {
                click: {
                    fn: me.reset,
                    scope: me
                }
            }
        };

        me.callParent(arguments);
    }
});