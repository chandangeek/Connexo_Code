Ext.define('Uni.view.search.field.NumberLine', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-view-search-field-number-line',
    width: '455',
    layout: 'hbox',
    items: [
        {
            xtype: 'combo',
            disabled: true,
            width: 55,
            margin: '1px 30px 1px 5px'
        },
        {
            xtype: 'numberfield',
            margin: '1px 5px 1px 0px',
            value: 0,
            width: 180,
            listeners: {
                change: function () {
                    me = this;
                    if (me.value === 0) {
                        me.up('container').down('button').disable(true)
                    } else {
                        me.up('container').down('button').enable(true)
                        me.up('button').setText(me.up('button').defaultText + '*')
                    }

                }
            }
        }

    ],
    rbar: {
        width: 30,
        items: [
            {
                xtype: 'button',
                itemId: 'filter-clear',
                ui: 'plain',
                tooltip: 'Clear filter',
                iconCls: ' icon-close4',
                margin: '0px 0px 5px 6px',
                disabled: true,
                hidden: true,
                style: {
                    fontSize: '16px'
                },
                handler: function () {
                    var me = this;
                    me.up('menu').focus().down('numberfield').reset();
                }
            }
        ]
    }

    ,
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
        me = this;
        me.callParent(arguments);
        me.down('combo').setValue(this.operator);
    }
});