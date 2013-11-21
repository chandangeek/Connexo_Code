Ext.define('Cfg.view.validation.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.validationrulesetList',
    itemId: 'validationrulesetList',
    title: 'All Rule Sets',
    store: 'ValidationRuleSets',
    columns: {
        defaults: {
            flex: 1
        },
        items: [
            { header: 'Name', dataIndex: 'name' },
            { header: 'Description', dataIndex: 'description' }
        ]
    },
    initComponent: function () {
        this.buttons = [
            {
                text: 'Save',
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
