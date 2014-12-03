Ext.define('Mdc.view.setup.deviceevents.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLogbookData',
    itemId: 'deviceLogbookData',
    requires: [
//        'Mdc.view.setup.deviceevents.SubMenuPanel',
//        'Mdc.view.setup.deviceevents.SideFilter',
        'Mdc.view.setup.deviceevents.DataSortingToolbar',
        'Mdc.view.setup.deviceevents.DataTableView',
        'Uni.component.filter.view.FilterTopPanel'
    ],
    toggleId: null,
    router: null,
    device: null,
    initComponent: function () {
        var me = this;
        me.content = {
            ui: 'large',
                title: me.title,
                items: [
                {
                    xtype: 'filter-top-panel',
                    itemId: 'device-logbook-data-filter-toolbar',
                    emptyText: Uni.I18n.translate('general.none', 'MDC', 'None'),
                    hideEmpty: false
                },
                {
                    xtype: 'menuseparator'
                },
                {
                    xtype: 'deviceLogbookDataSortingToolbar',
                    itemId: 'deviceLogbookDataSortingToolbar'
                },
                {
                    xtype: 'deviceLogbookDataTableView',
                    itemId: 'deviceLogbookDataTableView',
                    device: me.device,
                    router: me.router
                }
            ]
        };
        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('deviceregisterconfiguration.devices', 'MDC', 'Devices'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: me.toggleId
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});