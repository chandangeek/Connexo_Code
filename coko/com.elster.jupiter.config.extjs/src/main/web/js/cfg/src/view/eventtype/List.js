Ext.define('Cfg.view.eventtype.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.eventtypeList',
    itemId: 'eventtypeList',
    title: Uni.I18n.translate('eventtype.allEventTypes','CFG','All Event Types'),
    store: 'EventTypes',

    columns: {
        defaults: {
            flex: 1
        },
        items: [
            { header: 'Topic', dataIndex: 'topic' },
            { header: 'Component', dataIndex: 'component' },
            { header: 'Scope', dataIndex: 'scope' },
            { header: 'Category', dataIndex: 'category' },
            { header: 'Name', dataIndex: 'name' },
            {
                header: 'Publish',
                dataIndex: 'publish',
                xtype: 'checkcolumn',
                editor: {
                    xtype: 'checkbox',
                    cls: 'x-grid-checkheader-editor'
                }
            }
        ]
    },

    initComponent: function () {
        this.buttons = [
            {
                text: Uni.I18n.translate('general.save','CFG','Save'),
                action: 'save'
            }
        ];
        this.dockedItems = [
            {
                xtype: 'pagingtoolbar',
                store: this.store,
                dock: 'bottom',
                displayInfo: true,
                afterPageText: '',
                displayMsg: 'Displaying {0} - {1}'
            }
        ];
        this.listeners = {
            'afterrender': function (component) {
                component.down('#last').hide()
            },
            single: true
        };
        this.callParent(arguments);
    }
});
