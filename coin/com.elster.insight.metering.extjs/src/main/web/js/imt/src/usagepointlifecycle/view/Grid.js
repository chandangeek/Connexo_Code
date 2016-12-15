Ext.define('Imt.usagepointlifecycle.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usagepoint-life-cycles-grid',
    xtype: 'usagepoint-life-cycles-grid',
    store: 'Imt.usagepointlifecycle.store.UsagePointLifeCycles',
    router: null,

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.usagepointlifecycle.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                xtype: 'uni-default-column',
                header: Uni.I18n.translate('general.default', 'IMT', 'Default'),
                dataIndex: 'isDefault',
                width: 70
            },
            {
                header: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/usagepointlifecycles/usagepointlifecycle').buildUrl({usagePointLifeCycleId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Imt.privileges.UsagePointLifeCycle.configure,
                menu: {
                    xtype: 'usagepoint-life-cycles-action-menu',
                    itemId: 'lifeCycleActionMenu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                displayMsg: Uni.I18n.translate('usagePointLifeCycles.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} usage point life cycles'),
                displayMoreMsg: Uni.I18n.translate('usagePointLifeCycles.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} usage point life cycles'),
                emptyMsg: Uni.I18n.translate('usagePointLifeCycles.pagingtoolbartop.emptyMsg', 'IMT', 'There are no usage point life cycles to display'),
                dock: 'top',
                noBottomPaging: true,
                usesExactCount: true,
                items: [
                    {
                        xtype: 'button',
                        privileges: Imt.privileges.UsagePointLifeCycle.configure,
                        itemId: 'add-usagepoint-life-cycle-button',
                        text: Uni.I18n.translate('general.addUsagePointLifeCycle', 'IMT', 'Add usage point life cycle'),
                        href: me.router.getRoute('administration/usagepointlifecycles/add').buildUrl()
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

