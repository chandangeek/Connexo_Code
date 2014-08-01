Ext.define('Usr.view.group.privilege.FeatureList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.featureList',
    itemId: 'featureList',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Usr.store.Resources',
        'Usr.view.group.privilege.FeatureActionMenu'
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
                    header: Uni.I18n.translate('privilege.feature', 'USM', 'Resource'),
                    flex: 3,
                    renderer: function (value, metadata, record) {
                        var name = Uni.I18n.translate(record.get('name'), 'USM', record.get('name'));
                        if(record.get('selected') == 0){
                            return '<img src="../ext/packages/uni-theme-skyline/build/resources/images/grid/drop-no.png"/>&nbsp;' + name;
                        }
                        else{
                            if(record.privileges().data.items.length == record.get('selected')){
                                return '<img src="../ext/packages/uni-theme-skyline/build/resources/images/grid/drop-yes.png"/>&nbsp;' + name;
                            }
                            else{
                                return '<img src="../ext/packages/uni-theme-skyline/build/resources/images/tree/drop-above.png"/>&nbsp;' + name;
                            }
                        }
                    }
                },
                {
                    header: Uni.I18n.translate('privilege.description', 'USM', 'Description'),
                    flex: 3,
                    renderer: function (value, metadata, record) {
                        return Uni.I18n.translate(record.get('description'), 'USM', record.get('description'));
                    }
                },
                {
                    header: Uni.I18n.translate('privilege.permissions', 'USM', 'Privileges'),
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
                displayMsg: Uni.I18n.translate('privilege.feature.top', 'USM', 'Resources')
            }
        ];

        this.callParent();
    }
});