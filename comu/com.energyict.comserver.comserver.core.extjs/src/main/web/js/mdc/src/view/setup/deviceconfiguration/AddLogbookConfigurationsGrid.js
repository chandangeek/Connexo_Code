Ext.define('Mdc.view.setup.deviceconfiguration.AddLogbookConfigurationsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.add-logbook-configurations-grid',
    store: 'LogbookConfigurations',
    selType: 'checkboxmodel',
    selModel: {
        checkOnly: true,
        enableKeyNav: false,
        showHeaderCheckbox: false
    },
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: 'Name',
                dataIndex: 'name',
                flex: 5
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode'
            }
        ];

        me.dockedItems = [
            {
                layout: 'hbox',
                dock: 'top',
                items: [
                    {
                        xtype: 'text',
                        itemId: 'logbook-count',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('logbookConfigurations.manage', 'MDC', 'Manage logbook configurations'),
                        action: 'manage',
                        ui: 'link',
                        listeners: {
                            click: {
                                fn: function () {
                                    window.location.href = '#/administration/logbooktypes';
                                }
                            }
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

