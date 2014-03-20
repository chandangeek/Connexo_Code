Ext.define('Isu.view.administration.datacollection.issueautomaticcreationrules.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Ext.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-autocreation-rules-list',
    border: false,
    items: [
        {
            name: 'empty-text',
            border: false,
            hidden: true,
            html: '<h3>No rules found</h3>' +
                '<p>There are no rules have been created yet</p>'
        },
        {
            xtype: 'grid',
            store: 'Isu.store.AutoCreationRules',
            height: 285,
            columns: {
                defaults: {
                    sortable: false,
                    menuDisabled: true
                },
                items: [
                    {
                        header: 'Priority',
                        dataIndex: 'priority',
                        flex: 1
                    },
                    {
                        header: 'Active',
                        dataIndex: 'active',
                        flex: 1
                    },
                    {
                        header: 'Issue type',
                        dataIndex: 'type',
                        flex: 2
                    },
                    {
                        header: 'Issue reason',
                        dataIndex: 'reason',
                        flex: 2
                    },
                    {
                        header: 'Issue title',
                        dataIndex: 'title',
                        flex: 3
                    },
                    {
                        header: 'Rule',
                        dataIndex: 'rule',
                        flex: 2
                    },
                    {
                        header: 'Assignee',
                        xtype: 'templatecolumn',
                        tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type} isu-assignee-type-icon"></span></tpl> {assignee.name}',
                        flex: 2
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
            items: [
                {
                    xtype: 'pagingtoolbartop',
                    store: 'Isu.store.AutoCreationRules',
                    border: false
                }
            ]
        }
    ],

    initComponent: function () {
        var self = this,
            store;


        self.callParent(arguments);

        store = this.down('grid').getStore();

        store.on({
            load: {
                fn: self.onStoreLoad,
                scope: self
            }
        });

        store.load();

    }
});
