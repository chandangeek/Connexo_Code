Ext.define('Isu.view.administration.datacollection.issuecreationrules.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Ext.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-creation-rules-list',
    border: false,
    height: 365,
    items: [
        {
            name: 'empty-text',
            border: false,
            hidden: true,
            html: '<h3>No rule found</h3>' +
                '<p>No issue creation rules have been created yet.</p>' +
                '<p>Possible steps:</p>',
            bbar: {
                padding: 0,
                items: [
                    {
                        text: 'Create rule',
                        action: 'create'
                    }
                ]
            }
        },
        {
            xtype: 'grid',
            store: 'CreationRules',
            height: 285,
            columns: {
                defaults: {
                    sortable: false,
                    menuDisabled: true
                },
                items: [
                    {
                        header: 'Name',
                        dataIndex: 'name',
                        tdCls: 'isu-grid-description',
                        flex: 1
                    },
                    {
                        header: 'Rule template',
                        dataIndex: 'template',
                        tdCls: 'isu-grid-description',
                        flex: 1
                    },
                    {
                        header: 'Issue reason',
                        dataIndex: 'reason',
                        tdCls: 'isu-grid-description',
                        flex: 1
                    },
                    {
                        header: 'Assignee',
                        xtype: 'templatecolumn',
                        tpl: '<tpl if="assignee"><tpl if="assignee.type"><span class="isu-icon-{assignee.type} isu-assignee-type-icon"></span></tpl> {assignee.name}<tpl else>Automatic</tpl>',
                        flex: 1
                    },
                    {
                        header: 'Action',
                        xtype: 'actioncolumn',
                        iconCls: 'isu-action-icon',
                        align: 'left',
                        width: 70
                    }
                ]
            }
        }
    ],

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top',
            layout: 'hbox',
            items: [
                {
                    xtype: 'pagingtoolbartop',
                    store: 'CreationRules',
                    border: false,
                    flex: 1
                },
                {
                    xtype: 'button',
                    text: 'Create rule',
                    action: 'create'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            store: 'CreationRules',
            dock: 'bottom'
        }
    ],

    initComponent: function () {
        var self = this,
            store;

        self.callParent(arguments);

        store = this.down('grid').getStore();

        self.onStoreLoad(store);

        store.on({
            load: {
                fn: self.onStoreLoad,
                scope: self
            }
        });

        store.load();
    },

    onStoreLoad: function (store) {
        var storeTotal = store.getCount();

        if (storeTotal) {
            this.hideEmptyText();
        } else {
            this.showEmptyText();
        }
    },

    showEmptyText: function () {
        var grid = this.down('grid'),
            emtyText = this.down('panel[name=empty-text]');

        if (grid && emtyText) {
            grid.hide();
            emtyText.show();
        }
    },

    hideEmptyText: function () {
        var grid = this.down('grid'),
            emtyText = this.down('panel[name=empty-text]');

        if (grid && emtyText) {
            grid.show();
            emtyText.hide();
        }
    }
});
