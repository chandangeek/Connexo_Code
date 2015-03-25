Ext.define('Dlc.devicelifecycletransitions.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycle-transitions-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dlc.devicelifecycletransitions.view.Grid',
        'Dlc.devicelifecycletransitions.view.Preview',
        'Dlc.main.view.SideMenu'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'device-life-cycles-side-menu',
            router: me.router
        };

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.transitions', 'DLC', 'Transitions'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-life-cycle-transitions-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceLifeCycleTransitions.empty.title', 'DLC', 'No transitions found'),
                        reasons: [
                            Uni.I18n.translate('deviceLifeCycleTransitions.empty.list.item1', 'DLC', 'No transitions have been added yet')
                        ]
                    },
                    previewComponent: {
                        xtype: 'device-life-cycle-transitions-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

