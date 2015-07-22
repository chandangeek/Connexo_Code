Ext.define('Uni.view.search.field.RangeLine', {
    extend: 'Ext.container.Container',
    xtype: 'uni-view-search-field-range-line',
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
                    xtype: 'datefield',
                    margin: '1px 0px 0px 0px'
                },
                {
                    xtype: 'label',
                    text: 'at',
                    margin: '3px 15px 0px 25px'
                },
                {
                    flex: 0.1,
                    itemId: 'flex',

                },
                {
                    xtype: 'numberfield',
                    itemId: 'hours',
                    value: 0,
                    maxValue: 23,
                    minValue: 0,
                    width: 55

                },
                {
                    xtype: 'numberfield',
                    itemId: 'minutes',
                    value: 0,
                    maxValue: 59,
                    minValue: 0,
                    width: 55

                },
                {
                    xtype: 'button',
                    iconCls: ' icon-close2',
                    action: 'remove',
                    margin: '1px 0px 0px 12px',
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