Ext.define('Usr.view.group.privilege.FeatureList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.featureList',
    itemId: 'featureList',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Usr.store.Privileges',
        'Usr.view.group.privilege.FeatureActionMenu'
    ],

    store: 'Usr.store.Privileges',

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
                    header: Uni.I18n.translate('privilege.feature', 'USM', 'Feature'),
                    dataIndex: 'name',
                    flex: 7,
                    renderer: function (value, metadata, record) {
                        if(record.get('selected')){
                            return '<img src="../ext/packages/uni-theme-skyline/build/resources/images/grid/drop-yes.png"/>&nbsp;' + record.get('name');
                        }
                        else{
                            return '<img src="../ext/packages/uni-theme-skyline/build/resources/images/grid/drop-no.png"/>&nbsp;' + record.get('name');
                        }
                    }
                },
                {
                    header: Uni.I18n.translate('privilege.permissions', 'USM', 'Permissions'),
                    flex: 3,
                    renderer: function (value, b, record) {
                        var text = Uni.I18n.translate('privilege.deny', 'USM', 'Deny');
                        if(record.get('selected')){
                            text = Uni.I18n.translate('privilege.allow', 'USM', 'Allow');
                        }

                        if(record.isModified('selected')){
                            text = text + '<img src="../ext/packages/uni-theme-skyline/build/resources/images/grid/dirty-rtl.png"/>';
                        }

                        return text;
                    }
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
                displayMsg: Uni.I18n.translate('privilege.feature.top', 'USM', 'Privileges')
            }
        ];

        this.callParent();
    }
});