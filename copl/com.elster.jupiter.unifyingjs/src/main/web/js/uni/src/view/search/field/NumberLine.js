Ext.define('Uni.view.search.field.NumberLine', {
    extend: 'Ext.container.Container',
    xtype: 'uni-view-search-field-number-line',
    margin: '5px 5px 3px 5px',
    width: '477',
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
                    margin: '1px 40px 0px 0px'
                },
                {
                    xtype: 'numberfield',
                    margin: '1px 0px 0px 0px',
                    value: 0,
                    hideTrigger: true
                },
                {
                    xtype: 'button',
                    iconCls: ' icon-close2',
                    action: 'remove',
                    margin: '1px 0px 0px 12px',
                    handler: function () {
                        var me = this;
                        me.up('menu').focus().down('numberfield').reset();
                    }
                }
            ]
        }
    ]
});