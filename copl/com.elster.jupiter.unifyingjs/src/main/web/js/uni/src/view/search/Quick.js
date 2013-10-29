Ext.define('Uni.view.search.Quick', {
    extend: 'Ext.container.Container',
    alias: 'widget.searchQuick',
    cls: 'search-quick',
    layout: {
        type: 'hbox',
        align: 'stretch',
        pack: 'end'
    },
    items: [
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'textfield',
                    cls: 'search-field',
                    emptyText: 'Search'
                }
            ]
        },
        {
            xtype: 'button',
            cls: 'search-button',
            glyph: 'xe021@icomoon',
            scale: 'small'
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});