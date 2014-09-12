Ext.define('Mdc.view.setup.devicelogbooks.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLogbookData',
    itemId: 'deviceLogbookData',
    requires: [
        'Mdc.view.setup.devicelogbooks.SubMenuPanel',
        'Mdc.view.setup.devicelogbooks.SideFilter',
        'Mdc.view.setup.devicelogbooks.TopFilter',
        'Mdc.view.setup.devicelogbooks.DataSortingToolbar',
        'Mdc.view.setup.devicelogbooks.DataTableView'
    ],

    router: null,

    content: {
        ui: 'large',
        title: Uni.I18n.translate('devicelogbooks.data.header', 'MDC', 'Logbook data'),
        items: [
            {
                xtype: 'deviceLogbookDataTopFilter'
            },
            {
                xtype: 'menuseparator'
            },
            {
                xtype: 'deviceLogbookDataSortingToolbar'
            },
            {
                xtype: 'deviceLogbookDataTableView'
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'panel',
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'deviceLogbookSubMenuPanel',
                    router: me.router
                },
                {
                    xtype: 'deviceLogbookDataSideFilter'
                }
            ]
        };

        me.callParent(arguments);
    }
});