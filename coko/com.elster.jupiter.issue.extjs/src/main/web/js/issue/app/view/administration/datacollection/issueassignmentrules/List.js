Ext.define('Isu.view.administration.datacollection.issueassignmentrules.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Ext.grid.column.Action'
    ],
    alias: 'widget.issues-assignment-rules-list',
    border: false,
    items: [
        {
            name: 'empty-text',
            border: false,
            hidden: true,
            items: [
                {
                    html: '<h3>No rules found</h3>'
                },
                {
                    xtype: 'label',
                    text: 'There are no rules have been created yet'
                }
            ]
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
                        xtype: 'uni-actioncolumn',
                        items: 'Isu.view.administration.datacollection.issueassignmentrules.ActionMenu'
                    }
                ]
            },
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'top'
                }
            ]
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
            this.setTotal(storeTotal);
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
    },

    setTotal: function (total) {
        var grid = this.down('grid'),
            gridTop;

        if (grid) {
            gridTop = grid.getDockedItems('toolbar[dock="top"]')[0];
            gridTop.removeAll();
            gridTop.add({
                xtype: 'component',
                html: total + ' rule' + (total > 1 ? 's' : '')
            });
        }
    }
});

