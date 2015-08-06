Ext.define('Uni.view.search.field.internal.NumberLine', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-view-search-field-number-line',
    width: '455',
    layout: 'hbox',

    defaults: {
        margin: '0 10 0 0'
    },

    onChange: function(elm, value) {
        this.down('#filter-clear').setDisabled(!value);
        this.fireEvent('change', this, value);
    },

    onReset: function() {
        this.down('#filter-clear').setDisabled(true);
        this.down('numberfield').reset();
        this.fireEvent('reset');
    },

    listeners: {
        render: function (c) {
            var button = c.down('#filter-clear');
            c.el.on('mouseover', function () {
                button.setVisible(true);
            });
            c.el.on('mouseout', function () {
                button.setVisible(false);
            });
        }
    },

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
                xtype: 'numberfield',
                value: 0,
                width: 180,
                listeners: {
                    change:{
                        fn: me.onChange,
                        scope: me
                    }
                }
            }
        ];

        me.rbar = {
            width: 15,
            items: {
                xtype: 'button',
                itemId: 'filter-clear',
                ui: 'plain',
                tooltip: 'Clear filter',
                iconCls: ' icon-close4',
                disabled: true,
                hidden: true,
                style: {
                    fontSize: '16px'
                },
                handler: me.onReset,
                scope: me
            }
        };

        me.callParent(arguments);
    }
});