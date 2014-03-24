Ext.define('Isu.view.administration.datacollection.licensing.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.licensing-list',
    border: false,
    items: [
        {
            xtype: 'grid',
            store: 'Isu.store.Licensing',
            padding: '0 5 0 0',
            selType: 'checkboxmodel',
            selModel: {
                checkOnly: true,
                enableKeyNav: false,
                showHeaderCheckbox: false
            },
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
                        header: 'Expiration date',
                        dataIndex: 'expires',
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
            }
        }
    ],

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top',
            items: [
                {
                    xtype: 'container',
                    flex: 1,
                    items: [
                        {
                            xtype: 'pagingtoolbartop',
                            store: 'Isu.store.Licensing',
                            dock: 'top',
                            border: false
                        }
                    ]
                },
                {
                    xtype: 'button',
                    text: 'Add license',
                    action: 'addlicense',
                    hrefTarget: '',
                    href: '#/administration/datacollection/licensing/addlicense'
                }
            ]
        },
        {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [
                {
                    xtype: 'pagingtoolbarbottom',
                    store: 'Isu.store.Licensing',
                    border: false
                }
            ]
        }
    ],

    initComponent: function () {
        var self = this;
        self.callParent(arguments);
    }
});

