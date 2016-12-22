Ext.define('Imt.usagepointlifecyclestates.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-life-cycle-states-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.usagepointlifecyclestates.view.Grid',
        'Imt.usagepointlifecyclestates.view.Preview',
        'Imt.usagepointlifecycle.view.SideMenu'
    ],

    router: null,
    lifecycleRecord: null,

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'usagepoint-life-cycles-side-menu',
                    itemId: 'states-side-menu',
                    router: me.router
                }
            ]
        };

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.states', 'IMT', 'States'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'usagepoint-life-cycle-states-grid',
                        itemId: 'states-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-states-panel',
                        title: Uni.I18n.translate('usagePointLifeCycleStates.empty.title', 'IMT', 'No states found'),
                        reasons: [
                            Uni.I18n.translate('usagePointLifeCycleStates.empty.list.item1', 'IMT', 'No states have been added yet')
                        ],
                        stepItems: [
                            {
                                xtype: 'button',
                                itemId: 'add-state-button',
                                text: Uni.I18n.translate('usagePointLifeCycleStates.add', 'IMT', 'Add state'),
                                action: 'addState'                                
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'usagepoint-life-cycle-states-preview',
                        itemId: 'usagepoint-life-cycle-states-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

