Ext.define('Imt.usagepointlifecycletransitions.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usagepoint-life-cycle-transitions-grid',
    store: 'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitions',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.transition', 'IMT', 'Transition'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.from', 'IMT', 'From'),
                dataIndex: 'fromState_name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.to', 'IMT', 'To'),
                dataIndex: 'toState_name',
                flex: 1
            },            
            {
                xtype: 'uni-actioncolumn',
                privileges: Imt.privileges.UsagePointLifeCycle.configure,
                menu: {
                    xtype: 'transitions-action-menu',
                    itemId: 'transitions-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('usagePointLifeCycleTransitions.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} transitions'),
                displayMoreMsg: Uni.I18n.translate('usagePointLifeCycleTransitions.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} transitions'),
                emptyMsg: Uni.I18n.translate('usagePointLifeCycleTransitions.pagingtoolbartop.emptyMsg', 'IMT', 'There are no transitions to display'),
                items: [
                    {
                        xtype: 'button',
                        privileges: Imt.privileges.UsagePointLifeCycle.configure,
                        itemId: 'toolbar-button',
                        text: Uni.I18n.translate('general.addTransition', 'IMT', 'Add transition'),
                        href: me.router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/transitions/add').buildUrl()
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('usagePointLifeCycleTransitions.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Transitions per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

