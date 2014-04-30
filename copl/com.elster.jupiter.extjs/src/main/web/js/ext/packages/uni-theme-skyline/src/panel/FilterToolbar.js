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
    showClearButton: true,

    items: [
        {
            xtype: 'container',
            itemId: 'itemsContainer',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: []
        },
        {
            xtype: 'label',
            itemId: 'emptyLabel',
            hidden: true
        },
        {
            xtype: 'container',
            itemId: 'toolsContainer',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            dock: 'left'
        }
    ],

    dockedItems: [
        {
            xtype: 'header',
            dock: 'left'
        },
        {
            xtype: 'button',
            text: 'Clear all',
            action: 'clear',
            disabled: true,
            dock: 'right'
        }
    ],

    updateContainer: function(container) {
        var count = container.items.getCount();

        !count
            ? this.getEmptyLabel().show()
            : this.getEmptyLabel().hide()
        ;
        this.getClearButton().setDisabled(!count);
    },

    constructor: function (config) {
        var me = this;

        Ext.apply(config, me);

        this.dockedItems[0].title = me.title;

        this.items[0].items =  me.content;
        this.items[1].text = me.emptyText;
        this.items[2].items = me.tools;

        this.callSuper(arguments);
        if (!this.showClearButton) {
            this.getClearButton().hide();
        }
        this.getContainer().on('afterlayout', 'updateContainer', this);
    },

    getContainer: function() {
       return this.down('#itemsContainer')
    },

    getTools: function() {
        return this.down('#toolsContainer')
    },

    getClearButton: function() {
        return this.down('button[action="clear"]')
    },

    getEmptyLabel: function() {
        return this.down('#emptyLabel')
    }
});