Ext.define('Uni.view.search.field.NumberLine', {
    extend: 'Ext.container.Container',
    xtype: 'uni-view-search-field-number-line',
    margin: '5px 5px 3px 5px',
    width: '450',
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
                    xtype: 'numberfield',
                    margin: '1px 5px 0px 0px',
                    value: 0,
                    //hideTrigger: true,
                    listeners: {
                        change: function() {
                            me = this;
                            if (me.value === 0) {
                                me.up('container').down('button').disable(true)
                            } else {
                                me.up('container').down('button').enable(true)
                            }

                        }
                    }
                },
                {
                    xtype: 'button',
                    itemId: 'filter-clear',
                    ui: 'plain',
                    tooltip: 'Clear filter',
                    iconCls: ' icon-close4',
                    margin: '0px 0px 0px 5px',
                    padding: 6,
                    disabled: true,
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
    ]
});