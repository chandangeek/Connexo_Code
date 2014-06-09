Ext.define('Isu.view.administration.datacollection.issueassignmentrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Ext.grid.column.Action'
    ],
    alias: 'widget.issues-assignment-rules-list',
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
    ],


    initComponent: function () {
        var self = this,
            store;

        self.callParent(arguments);

        store = self.getStore();

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
        }
    },

    setTotal: function (total) {
        var self = this,
            gridTop;

        if (self) {
            gridTop = self.getDockedItems('toolbar[dock="top"]')[0];
            gridTop.removeAll();
            gridTop.add({
                xtype: 'component',
                html: total + ' rule' + (total > 1 ? 's' : '')
            });
        }
    }
});

