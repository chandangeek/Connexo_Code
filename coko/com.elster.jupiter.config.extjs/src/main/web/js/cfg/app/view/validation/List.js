Ext.define('Cfg.view.validation.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.validationrulesetList',
    itemId: 'validationrulesetList',
    title: 'All Rule Sets',
    store: 'ValidationRuleSets',
    tbar: [{
        text: 'Add Rule Set',
        itemId: 'addRuleSet',
        action: 'addRuleSet'
    },
        {
            itemId: 'removeRuleSet',
            text: 'Remove Rule Set',
            action: 'removeRuleSet',
            disabled: true
        }],
    listeners: {
        'selectionchange': function(view, records) {
            rulesGrid.down('#removeRuleSet').setDisabled(!records.length);
        }
    },
    columns: {
        defaults: {
            flex: 1
        },
        items: [
            //{ header: 'Id', dataIndex: 'id', flex: 0.1 },
            { header: 'Name', dataIndex: 'name' },
            { header: 'Description', dataIndex: 'description' }
        ]
    },
    initComponent: function () {
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
