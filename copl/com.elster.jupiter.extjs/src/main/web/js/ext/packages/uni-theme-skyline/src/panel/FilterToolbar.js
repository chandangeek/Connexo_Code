Ext.define('Skyline.panel.FilterToolbar', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.filter-toolbar',
    titlePosition: 'left',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    header: false,
    ui: 'filter-toolbar',

    dockedItems: [
        {
            xtype: 'header',
            title: this.title,
            dock: 'left'
        },
        {
            xtype: 'button',
            text: 'Clear all',
            action: 'clear',
            disabled: true,
            dock: 'right'
        },
        {
            xtype: 'panel',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            dock: 'left'
        }
    ],

    constructor: function (config) {
        var dockedItems = config.dockedItems;
        config.dockedItems = [];

        this.dockedItems[0].title = config.title;
        this.dockedItems[2].items = dockedItems;
        Ext.apply(config, this);

        this.callSuper(arguments);
    }
});