Ext.define('Isu.view.workspace.datacollection.issueassignmentrules.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.grid.column.Template',
        'Ext.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-assignment-rules-list',
    border: false,
    items: [
        {
            name: 'empty-text',
            border: false,
            hidden: true,
            html: '<h3>No rules found</h3>' +
                '<p>There are no rules. This could be because:</p>' +
                '<ul>' +
                '<li>No rules have been created yet</li>' +
                '<li>The filter is too narrow</li>' +
                '</ul>' +
                '<p>Possible steps:</p>',
            bbar: [
                {
                    text: 'Create rule',
                    name: 'create-issues-assignment-rules'
                }
            ]
        },
        {
            xtype: 'grid',
            store: 'Isu.store.AssignmentRules',
            height: 285,
            columns: [
                {
                    header: 'Priority',
                    dataIndex: 'priority',
                    sortable: false,
                    menuDisabled: true,
                    align: 'right',
                    width: 90
                },
                {
                    header: 'Name',
                    dataIndex: 'name',
                    sortable: false,
                    menuDisabled: true,
                    flex: 1
                },
                {
                    header: 'Assign to',
                    xtype: 'templatecolumn',
                    tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type}"></span></tpl> {assignee.title}',
                    sortable: false,
                    menuDisabled: true,
                    flex: 1
                },
                {
                    header: 'Status',
                    dataIndex: 'status',
                    sortable: false,
                    menuDisabled: true,
                    width: 100
                },
                {
                    header: 'Actions',
                    xtype: 'actioncolumn',
                    iconCls: 'isu-action-icon',
                    align: 'left',
                    sortable: false,
                    menuDisabled: true,
                    width: 70
                }
            ]
        }
    ],

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top',
            items: [
                {
                    xtype: 'container',
                    flex: 1/*,
                    items: [
                        {
                            xtype: 'pagingtoolbartop',
                            store: 'Isu.store.AssignmentRules',
                            border: false
                        }
                    ]*/
                },
                {
                    xtype: 'button',
                    name: 'create-issues-assignment-rules',
                    text: 'Create assignment rule'
                },
                {
                    xtype: 'button',
                    name: 'bulk-change-issues-assignment-rules',
                    text: 'Bulk action',
                    disabled: true
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

//        store.load();
    },

    onStoreLoad: function (store) {
        if (store.getTotalCount()) {
            this.hideEmptyText();
        } else {
            this.showEmptyText();
        }
    },

    showEmptyText: function () {
        this.down('button[name=bulk-change-issues-assignment-rules]').setDisabled(true);
        this.down('grid').hide();
        this.down('panel[name=empty-text]').show();
    },

    hideEmptyText: function () {
        this.down('button[name=bulk-change-issues-assignment-rules]').setDisabled(false);
        this.down('grid').show();
        this.down('panel[name=empty-text]').hide();
    }
});

