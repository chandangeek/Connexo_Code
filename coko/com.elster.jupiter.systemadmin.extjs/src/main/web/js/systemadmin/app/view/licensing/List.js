Ext.define('Sam.view.licensing.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Column'
    ],
    alias: 'widget.licensing-list',
    border: false,
    items: [
        {
            itemId: 'noLicenseFound',
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
                            items: [
                                {
                                    itemID: 'noLicenses',
                                    xtype: 'label',
                                    text: '0 licenses'
                                }
                            ]
                        },
                        {
                            itemId: 'uploadLicense',
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
            itemId: 'LicensingGrid',
            xtype: 'grid',
            store: 'Sam.store.Licensing',
            height: 285,
            columns: {
                defaults: {
                    sortable: false,
                    menuDisabled: true
                },
                items: [
                    {
                        itemId: 'License',
                        header: 'License',
                        dataIndex: 'applicationname',
                        flex: 5
                    },
                    {
                        itemId: 'Status',
                        header: 'Status',
                        dataIndex: 'status',
                        flex: 2
                    },
                    {
                        itemId: 'expirationDate',
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
                    itemId: 'toolbarTop',
                    xtype: 'toolbar',
                    dock: 'top',
                    items: [
                        {
                            xtype: 'container',
                            name: 'gridcontainer'
                        },
                        {
                            itemId: 'uploadButton',
                            xtype: 'button',
                            text: 'Upload licenses',
                            action: 'uploadlicenses',
                            hrefTarget: '',
                            href: '#/administration/licensing/upload'
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
                xtype: 'label',
                text: storeTotal + ' licenses'
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


