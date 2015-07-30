Ext.define('Uni.view.search.field.DateLine', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-view-search-field-date-line',
    layout: 'hbox',
    width: '440',
    items: [
        {
            xtype: 'combo',
            disabled: true,
            width: 55,
            margin: '1px 30px 1px 5px'
        },
        {
            xtype: 'datefield',
            margin: '1px 5px 1px 0px',
            listeners: {
                change: function () {
                    me = this;
                    if (me.value === null) {
                        me.up('container').down('button').disable(true)
                    } else {
                        me.up('container').down('button').enable(true);
                        me.up('button').setText(me.up('button').defaultText + '*');
                    }

                }
            }
        },
        {
            xtype: 'label',
            text: 'at',
            margin: '5px 10px 0px 20px'
        },
        {
            xtype: 'numberfield',
            itemId: 'hours',
            value: 0,
            maxValue: 23,
            minValue: 0,
            width: 55,
            margin: '1px 10px 1px 0px',
            listeners: {
                change: function () {
                    me = this;
                    if (me.value === 0 || me.hidden) {
                        me.up('container').down('button').disable(true)
                    } else {
                        me.up('container').down('button').enable(true);
                        me.up('button').setText(me.up('button').defaultText + '*');
                    }

                }
            }

        },
        {
            xtype: 'numberfield',
            itemId: 'minutes',
            value: 0,
            maxValue: 59,
            minValue: 0,
            width: 55,
            margin: '1px 5px 1px 0px',
            listeners: {
                change: function () {
                    me = this;
                    if (me.value === 0 || me.hidden) {
                        me.up('container').down('button').disable(true)
                    } else {
                        me.up('container').down('button').enable(true);
                        me.up('button').setText(me.up('button').defaultText + '*');
                    }

                }
            }

        },
        {
            itemId: 'flex',
            hidden: true,
            flex: 0.09,
        }],
    rbar: {
        width: 30,
        items: [
            {
                xtype: 'button',
                itemId: 'filter-clear',
                ui: 'plain',
                tooltip: 'Clear filter',
                iconCls: ' icon-close4',
                margin: '0px 2px 5px 6px',
                disabled: true,
                hidden: true,
                style: {
                    fontSize: '16px'
                },
                handler: function () {
                    var me = this;
                    me.up('uni-view-search-field-date-line').down('datefield').reset();
                    me.up('uni-view-search-field-date-line').down('#hours').reset();
                    me.up('uni-view-search-field-date-line').down('#minutes').reset();
                }
            }

        ]
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
        me = this;
        me.callParent(arguments);
        me.down('combo').setValue(this.operator);
        if(this.hideTime) {
            me.down('label').hide();
            me.down('#hours').hide();
            me.down('#minutes').hide();
            me.down('#flex').show();
        }
    }
});