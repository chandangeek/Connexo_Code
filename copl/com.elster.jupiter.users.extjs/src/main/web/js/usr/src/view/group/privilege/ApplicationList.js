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
                        var translate = Uni.I18n.translate(record.get('componentName'), 'USR', record.get('componentName'));
                        switch (record.get('selected')) {
                            case 0:
                                metadata.tdCls = 'uni-icon-drop-no';
                                break;
                            case 1:
                                metadata.tdCls = 'uni-no-icon';
                                break;
                            case 2:
                                metadata.tdCls = 'uni-icon-drop-yes';
                                break;
                        }
                        return translate;
                    }
                },
                {
                    header: Uni.I18n.translate('general.description', 'USR', 'Description'),
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
                    {
                        text: Uni.I18n.translate('privilege.noAccess', 'USR', 'No access'),
                        icon: '../sky/build/resources/images/grid/drop-no.png',
                        itemId: 'privilegesNoAccess',
                        action: 'privilegesNoAccess'
                    },
                    {
                        text: Uni.I18n.translate('privilege.fullControl', 'USR', 'Full control'),
                        icon: '../sky/build/resources/images/grid/drop-yes.png',
                        itemId: 'privilegesFullControl',
                        action: 'privilegesFullControl'
                    }
                ]
            }
        ];

        this.callParent();
    }
});