Ext.define('Usr.view.group.privilege.FeatureList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.featureList',
    itemId: 'featureList',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Usr.store.Resources',
        'Usr.view.group.privilege.FeatureActionMenu',
        'Uni.grid.column.Action'
    ],

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
                    flex: 3,
                    renderer: function (value, metadata, record) {
                        var name = Uni.I18n.translate(record.get('name'), 'USR', record.get('name'));
                        if (record.get('selected') == 0) {
                            metadata.tdCls = 'uni-icon-drop-no';
                        } else {
                            if (record.privileges().data.items.length == record.get('selected')) {
                                metadata.tdCls = 'uni-icon-drop-yes';
                            } else {
                                metadata.tdCls = 'uni-no-icon';
                            }
                        }
                        return name;
                    }
                },
                {
                    header: Uni.I18n.translate('general.description', 'USR', 'Description'),
                    flex: 3,
                    renderer: function (value, metadata, record) {
                        return Uni.I18n.translate(record.get('description'), 'USR', record.get('description'));
                    }
                },
                {
                    header: Uni.I18n.translate('privilege.permissions', 'USR', 'Privileges'),
                    flex: 7,
                    dataIndex: 'permissions'
                },
                {
                    xtype: 'uni-actioncolumn',
                    items: 'Usr.view.group.privilege.FeatureActionMenu'
                }
            ]
        };

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('privilege.feature.top', 'USR', 'Resources')
            }
        ];

        this.callParent();
    }
});