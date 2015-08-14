Ext.define('Uni.view.search.field.internal.NumberLine', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-numberline',
    width: '455',
    layout: 'hbox',

    defaults: {
        margin: '0 10 0 0'
    },

    removable: false,

    getValue: function() {
        return this.down('#filter-input').getValue();
    },

    reset: function() {
        this.down('#filter-input').reset();
        this.fireEvent('reset', this);
    },

    onChange: function(elm, value) {
        this.fireEvent('change', this, value);
    },

    onRemove: Ext.emptyFn,

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.items = [
            {
                xtype: 'combo',
                disabled: true,
                width: 55,
                value: me.operator
            },
            {
                xtype: 'textfield',
                itemId: 'filter-input',
                width: 180,
                margin: '0 5 0 0',
                listeners: {
                    change:{
                        fn: me.onChange,
                        scope: me
                    }
                }
            }
        ];

        if (me.removable) {
            me.rbar = {
                width: 15,
                items: {
                    xtype: 'button',
                    itemId: 'filter-clear',
                    ui: 'plain',
                    tooltip: 'Remove filter',
                    iconCls: ' icon-close4',
                    margin: '0 10 0 0',
                    hidden: true,
                    style: {
                        fontSize: '16px'
                    },
                    handler: me.onRemove,
                    scope: me
                }
            };
        }


        me.callParent(arguments);

        if (me.removable) {
            me.on('render', function() {
                var button = me.down('#filter-clear');
                me.getEl().on('mouseover', function () {
                    button.setVisible(true);
                });
                me.getEl().on('mouseout', function () {
                    button.setVisible(false);
                });
            });
        }
    }
});