Ext.define('Usr.view.group.privilege.ApplicationList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.applicationList',
    itemId: 'applicationList',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Usr.store.Applications',
        'Usr.view.group.privilege.ApplicationActionMenu',
        'Uni.grid.column.Action'
    ],

    store: 'Usr.store.Applications',

    initComponent: function () {
        this.columns = {
            defaults: {
                flex: 1,
                sortable: false,
                hideable: false,
                fixed: true
            },
            items: [
                {
                    header: Uni.I18n.translate('privilege.application', 'USR', 'Application'),
                    dataIndex: 'componentName',
                    flex: 3,
                    renderer: function (value, metadata, record) {
                        var text = [],
                            translate = Uni.I18n.translate(record.get('componentName'), 'USR', record.get('componentName'));

                        switch (record.get('selected')) {
                            case 0:
                                text = '<img src="../sky/build/resources/images/grid/drop-no.png"/>&nbsp;' + translate;
                                break;
                            case 1:
                                text = '<img src="../sky/build/resources/images/tree/drop-above.png" style="visibility:hidden"/>&nbsp;' + translate;
                                break;
                            case 2:
                                text = '<img src="../sky/build/resources/images/grid/drop-yes.png"/>&nbsp;' + translate;
                                break;
                        }
                        return text;
                    }
                },
                {
                    header: Uni.I18n.translate('privilege.description', 'USR', 'Description'),
                    dataIndex: 'description',
                    flex: 10
                },
                {
                    xtype: 'uni-actioncolumn',
                    items: 'Usr.view.group.privilege.ApplicationActionMenu'
                }
            ]
        };

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('privilege.application.top', 'USR', 'Applications'),
                items: [
                    '->',
                    {
                        text: Uni.I18n.translate('privilege.no.access', 'USR', 'No access'),
                        icon: '../sky/build/resources/images/grid/drop-no.png',
                        action: 'privilegesNoAccess'
                    },
                    {
                        text: Uni.I18n.translate('privilege.full.control', 'USR', 'Full control'),
                        icon: '../sky/build/resources/images/grid/drop-yes.png',
                        action: 'privilegesFullControl'
                    }
                ]
            }
        ];

        this.callParent();
    }
});