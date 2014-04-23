Ext.define('Sam.view.licensing.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Column'
    ],
    alias: 'widget.licensing-list',
    border: false,
    items: [
        {
            name: 'empty-text',
            cls: 'license-list-empty',
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
                            text: 'Upload licenses',
                            action: 'uploadlicenses',
                            hrefTarget: '',
                            href: '#/sysadministration/licensing/upload'
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'grid',
            store: 'Sam.store.Licensing',
            cls: 'license-list-grid',
            height: 285,
            columns: {
                defaults: {
                    sortable: false,
                    menuDisabled: true
                },
                items: [
                    {
                        header: 'License',
                        dataIndex: 'applicationname',
                        flex: 5
                    },
                    {
                        header: 'Status',
                        dataIndex: 'status',
                        flex: 2
                    },
                    {
                        header: 'Expiration date',
                        xtype: 'datecolumn',
                        format: 'd-m-Y',
                        dataIndex: 'expires',
                        sortable: true,
                        flex: 2
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
                            text: 'Upload licenses',
                            action: 'uploadlicenses',
                            hrefTarget: '',
                            href: '#/sysadministration/licensing/upload'
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


