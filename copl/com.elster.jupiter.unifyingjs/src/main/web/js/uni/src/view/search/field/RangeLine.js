Ext.define('Uni.view.search.field.RangeLine', {
    extend: 'Ext.container.Container',
    xtype: 'uni-view-search-field-range-line',
    margin: '5px 5px 3px 5px',
    width: '440',
    items: [
        {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    xtype: 'combo',
                    value: '>',
                    disabled: true,
                    width: 55,
                    margin: '1px 30px 0px 5px'
                },
                {
                    xtype: 'datefield',
                    margin: '1px 0px 0px 0px',
                    listeners: {
                        change: function() {
                            me = this;
                            if (me.value === null) {
                                me.up('container').down('button').disable(true)
                            } else {
                                me.up('container').down('button').enable(true)
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
                    margin: '0px 10px 0px 0px',
                    listeners: {
                        change: function() {
                            me = this;
                            if (me.value === 0 || me.hidden) {
                                me.up('container').down('button').disable(true)
                            } else {
                                me.up('container').down('button').enable(true)
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
                    listeners: {
                        change: function() {
                            me = this;
                            if (me.value === 0 || me.hidden) {
                                me.up('container').down('button').disable(true)
                            } else {
                                me.up('container').down('button').enable(true)
                            }

                        }
                    }

                },
                {
                    itemId: 'flex',
                    hidden: true,
                    flex: 0.05,
                },
                {
                    xtype: 'button',
                    itemId: 'filter-clear',
                    ui: 'plain',
                    disabled: true,
                    tooltip: 'Clear filter',
                    iconCls: ' icon-close4',
                    margin: '0px 0px 0px 5px',
                    padding: 6,
                    style: {
                        fontSize: '16px'
                    },
                    handler: function () {
                        var me = this;
                        me.up('menu').focus().down('datefield').reset();
                        me.up('menu').focus().down('#hours').reset();
                        me.up('menu').focus().down('#minutes').reset();
                    }
                }
            ]
        }
    ]
});