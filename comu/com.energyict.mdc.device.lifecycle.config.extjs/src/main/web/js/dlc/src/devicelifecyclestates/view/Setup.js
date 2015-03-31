Ext.define('Dlc.devicelifecyclestates.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycle-states-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dlc.devicelifecyclestates.view.Grid',
        'Dlc.devicelifecyclestates.view.Preview',
        'Dlc.main.view.SideMenu'
    ],

    router: null,
    lifecycleRecord: null,

    initComponent: function () {
        var me = this;


        me.side = {
            xtype: 'device-life-cycles-side-menu',
            router: me.router
        };

        me.content = {
            ui: 'large',
            title: me.lifecycleRecord.get('name'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-life-cycle-states-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceLifeCycleStates.empty.title', 'DLC', 'No states found'),
                        reasons: [
                            Uni.I18n.translate('deviceLifeCycleStates.empty.list.item1', 'DLC', 'No states have been added yet')
                        ],
                        stepItems: [
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('deviceLifeCycleStates.add', 'DLC', 'Add state'),
                                action: 'addState'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'device-life-cycle-states-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

