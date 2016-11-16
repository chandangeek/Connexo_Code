Ext.define('Imt.usagepointlifecyclestates.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usagepoint-life-cycle-states-grid',
    store: 'Imt.usagepointlifecyclestates.store.UsagePointLifeCycleStates',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.usagepointlifecyclestates.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                xtype: 'uni-default-column',
                header: Uni.I18n.translate('general.initial', 'IMT', 'Initial'),
                dataIndex: 'isInitial',
                width: 70
            },
            {
                header: Uni.I18n.translate('general.states', 'IMT', 'States'),
                dataIndex: 'name',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',                
                privileges: Imt.privileges.UsagePointLifeCycle.configure,
                menu: {
                    xtype: 'usagepoint-life-cycle-states-action-menu',
                    itemId: 'statesActionMenu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('usagePointLifeCycleStates.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} states'),
                displayMoreMsg: Uni.I18n.translate('usagePointLifeCycleStates.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} states'),
                emptyMsg: Uni.I18n.translate('usagePointLifeCycleStates.pagingtoolbartop.emptyMsg', 'IMT', 'There are no states to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'add-state-button',
                        text: Uni.I18n.translate('usagePointLifeCycleStates.add', 'IMT', 'Add state'),
                        action: 'addState',                        
                        privileges: Imt.privileges.UsagePointLifeCycle.configure
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('usagePointLifeCycleStates.pagingtoolbarbottom.itemsPerPage', 'IMT', 'States per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

