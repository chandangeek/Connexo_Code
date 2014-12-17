Ext.define('Mdc.view.setup.deviceevents.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLogbookData',
    itemId: 'deviceLogbookData',
    requires: [
        'Mdc.view.setup.devicelogbooks.SideFilter',
        'Mdc.view.setup.deviceevents.DataSortingToolbar',
        'Mdc.view.setup.deviceevents.DataTableView',
        'Uni.component.filter.view.FilterTopPanel'
    ],
    toggleId: null,
    router: null,
    title: null,
    device: null,
    eventsView: null,
    side: true,
    initComponent: function () {
        var me = this,
            title = null;
        if (me.eventsView) {
            title = me.title
        }
        me.content = {
            ui: 'large',
            title: title,
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
                    router: me.router,
                    eventsView: me.eventsView
                }
            ]
        };
        if (me.side) {
            me.side = [
                {
                    xtype: 'panel',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    items: [
                        {
                            ui: 'medium',
                            items: [
                                {
                                    xtype: 'deviceMenu',
                                    itemId: 'stepsMenu',
                                    device: me.device,
                                    toggleId: me.toggleId
                                }
                            ]
                        },
                        {
                            xtype: 'deviceLogbookDataSideFilter',
                            itemId: 'deviceLogbookDataSideFilter'
                        }
                    ]
                }
            ]
        }
        me.callParent(arguments);
    }
});