Ext.define('Isu.view.administration.datacollection.licensing.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Action',
        'Ext.grid.column.Template'
    ],
    alias: 'widget.licensing-list',
    border: false,
    items: [
        {
            name: 'empty-text',
            margin: '10 0 0 0',
            border: false,
            hidden: true,
            html: '<br><h3>No licenses found</h3>' +
                '<p>No licenses have been uploaded yet</p>',
            items: [
                {
                    xtype: 'toolbar',
                    dock: 'top',
                    items: [
                        {
                            xtype: 'container',
                            flex: 1,
                            items: [
                                {
                                html: '0 licenses'
                                }
                            ]
                        },
                        {
                            xtype: 'button',
                            text: 'Add license',
                            action: 'addlicense',
                            hrefTarget: '',
                            href: '#/issue-administration/datacollection/licensing/addlicense'
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'grid',
            store: 'Isu.store.Licensing',
            padding: '0 5 0 0',
            selType: 'checkboxmodel',
            height: 285,
            columns: {
                defaults: {
                    sortable: false,
                    menuDisabled: true
                },
                items: [
                    {
                        header: 'License',
                        dataIndex: 'application',
                        flex: 5
                    },
                    {
                        header: 'Status',
                        dataIndex: 'status',
                        flex: 1
                    },
                    {
                        header: 'Expiration date',
                        xtype: 'templatecolumn',
                        dataIndex: 'expires',
                        tpl: new Ext.XTemplate(
                            '{[this.isString(values.expires) ? "Never expires" : this.func(values.expires)]}',
                            {
                                isString: function (exp) {
                                    return exp == 'never';
                                },
                                func: function (date) {
                                    return Ext.Date.format(new Date(parseInt(date)), 'M d Y');
                                }
                            }
                        ),
                        sortable: true,
                        flex: 2
                    },
                    {
                        header: 'Actions',
                        xtype: 'actioncolumn',
                        iconCls: 'isu-action-icon',
                        align: 'left',
                        width: 70
                    }
                ]
            },
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'top',
                    items: [
                        {
                            xtype: 'container',
                            name: 'gridcontainer',
                            flex: 1
                        },
                        {
                            xtype: 'button',
                            text: 'Add license',
                            action: 'addlicense',
                            hrefTarget: '',
                            href: '#/issue-administration/datacollection/licensing/addlicense'
                        }
                    ]
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
        var storeTotal = store.getTotalCount(),
            gridTop = Ext.ComponentQuery.query('container[name="gridcontainer"]')[0];
        if (storeTotal) {
            gridTop.removeAll();
            gridTop.add({
                html: storeTotal + ' licenses'
            });
            this.hideEmptyText();
        } else {
            this.showEmptyText();
        }
    },

    showEmptyText: function () {
        var grid = this.down('grid'),
            emptyText = this.down('panel[name="empty-text"]');
        if (grid && emptyText) {
            grid.hide();
            emptyText.show();
        }
    },

    hideEmptyText: function () {
        var grid = this.down('grid'),
            emptyText = this.down('panel[name="empty-text"]');
        if (grid && emptyText) {
            grid.show();
            emptyText.hide();
        }
    }
});

