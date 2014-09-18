Ext.define('Uni.view.panel.FilterToolbar', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.filter-toolbar',
    titlePosition: 'left',
    layout: {
        type: 'hbox'
    },
    header: false,
    ui: 'filter-toolbar',
    showClearButton: true,

    items: [
        {
            xtype: 'container',
            itemId: 'itemsContainer',
            defaults: {
                margin: '0 8 0 0'
            },
            items: []
        },
        {
            xtype: 'label',
            itemId: 'emptyLabel',
            hidden: true
        },
        {
            xtype: 'component',
            flex: 1,
            html: '&nbsp;'
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
            xtype: 'container',
            dock: 'right',
            items: {
                itemId: 'Reset',
                xtype: 'button',
                text: 'Clear all',
                action: 'clear'
            }
        }
    ],

    updateContainer: function (container) {
        var count = container.items.getCount();
        !count || count < 1 ? this.hide() : this.show();
    },

    initComponent: function () {
        var me = this;

        this.dockedItems[0].title = me.title;
        this.items[0].items = me.content;
        this.items[1].text = me.emptyText;
        this.items[3].items = me.tools;

        this.callParent(arguments);

        this.getClearButton().on('click', function () {
            me.fireEvent('clearAllFilters');
        });

        if (!this.showClearButton) {
            this.getClearButton().hide();
        }

        this.getContainer().on('afterlayout', 'updateContainer', this);
    },

    getContainer: function () {
        return this.down('#itemsContainer')
    },

    getTools: function () {
        return this.down('#toolsContainer')
    },

    getClearButton: function () {
        return this.down('button[action="clear"]')
    },

    getEmptyLabel: function () {
        return this.down('#emptyLabel')
    }
});