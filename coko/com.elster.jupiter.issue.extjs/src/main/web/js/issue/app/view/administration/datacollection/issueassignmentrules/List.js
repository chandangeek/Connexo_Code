Ext.define('Isu.view.administration.datacollection.issueassignmentrules.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Column',
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
                '<p>There are no rules have been created yet</p>'
        },
        {
            xtype: 'grid',
            store: 'Isu.store.AssignmentRules',
            height: 285,
            columns: {
                defaults: {
                    sortable: false,
                    menuDisabled: true
                },
                items: [
                    {
                        header: 'Description',
                        dataIndex: 'description',
                        tdCls: 'isu-grid-description',
                        flex: 1
                    },
                    {
                        header: 'Assign to',
                        xtype: 'templatecolumn',
                        tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type} isu-assignee-type-icon"></span></tpl> {assignee.name}',
                        flex: 1
                    },
                    {
                        header: 'Actions',
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
                    store: 'Isu.store.AssignmentRules',
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

