Ext.define('Usr.view.group.privilege.FeatureList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.featureList',
    itemId: 'featureList',
    showTooltips: true,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Usr.store.Resources',
        'Usr.view.group.privilege.FeatureActionMenu',
        'Uni.grid.column.Action'
    ],

    viewConfig:{
        showTooltips: true
    },
    store: 'Usr.store.Resources',

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
                    header: Uni.I18n.translate('privilege.feature', 'USR', 'Resource'),
                    flex: 2,
                    dataIndex: 'name',
                    renderer: function (value, metadata, record) {
                        if (record.get('selected') == 0) {
                            metadata.tdCls = 'uni-icon-drop-no';
                        } else {
                            if (record.privileges().data.items.length == record.get('selected')) {
                                metadata.tdCls = 'uni-icon-drop-yes';
                            } else {
                                metadata.tdCls = 'uni-no-icon';
                            }
                        }
                        return value;
                    }
                },
                {
                    header: Uni.I18n.translate('general.description', 'USR', 'Description'),
                    flex: 2,
                    dataIndex: 'description'
                },
                {
                    header: Uni.I18n.translate('privilege.permissions', 'USR', 'Privileges'),
                    flex: 5,
                    dataIndex: 'permissions'
                },
                {
                    xtype: 'uni-actioncolumn',
                    menu: {
                        xtype: 'feature-action-menu'
                    }
                }
            ]
        };

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('privilege.resources.top', 'USR', 'Resources')
            }
        ];

        this.callParent();
    }
});