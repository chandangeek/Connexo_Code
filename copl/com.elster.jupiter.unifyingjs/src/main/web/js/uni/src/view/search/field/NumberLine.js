Ext.define('Uni.view.search.field.NumberLine', {
    extend: 'Ext.container.Container',
    xtype: 'uni-view-search-field-number-line',
    margin: '5px 5px 3px 5px',
    width: '477',
    removeHandler: function () {
        var me = this;
        debugger;
        me.down('menu').remove(me.down('menu').focus());
    },
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
                    margin: '3px 0px 0px 10px',
                    handler: function () {
                        var me = this;
                        me.up('menu').remove(me.up('menu').focus());
                    }
                }
            ]
        }
    ]
});