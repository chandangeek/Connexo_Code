Ext.define('Usr.view.group.privilege.ApplicationList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.applicationList',
    itemId: 'applicationList',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Usr.store.Applications',
        'Usr.view.group.privilege.ApplicationActionMenu'
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
                    header: Uni.I18n.translate('privilege.application', 'USM', 'Application'),
                    dataIndex: 'componentName',
                    flex: 3,
                    renderer: function (value, metadata, record) {
                        var text = [];
                        switch(record.get('rights')){
                            case 0: {
                                text = '<img src="../ext/packages/uni-theme-skyline/build/resources/images/grid/drop-no.png"/>&nbsp;' + record.get('componentName');
                                break;
                            }
                            case 1:{
                                text = '<img src="../ext/packages/uni-theme-skyline/build/resources/images/tree/drop-above.png"/>&nbsp;' + record.get('componentName');
                                break;
                            }
                            case 2:{
                                text = '<img src="../ext/packages/uni-theme-skyline/build/resources/images/grid/drop-yes.png"/>&nbsp;' + record.get('componentName');
                                break;
                            }
                        }
                        return text;
                    }
                },
                {
                    header: Uni.I18n.translate('privilege.description', 'USM', 'Description'),
                    dataIndex: 'description',
                    flex: 7
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
                displayMsg: Uni.I18n.translate('privilege.application.top', 'USM', 'Applications'),
                items: [
                    '->',
                    {
                        text: Uni.I18n.translate('privilege.no.access', 'USM', 'No access'),
                        action: 'privilegesNoAccess'
                    },
                    {
                        text: Uni.I18n.translate('privilege.full.control', 'USM', 'Full control'),
                        action: 'privilegesFullControl'
                    }
                ]
            }
        ];

        this.callParent();
    }
});