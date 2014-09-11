Ext.define('Mdc.view.setup.devicetype.AddLogbookTypes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-logbook-types',
    deviceTypeId: null,
    requires: [
        'Uni.grid.column.Obis'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'addLogbookPanel',
            items: [
                {
                    xtype: 'toolbar',
                    border: 0,
                    aling: 'left',
                    items: [
                        {
                            xtype: 'label',
                            name: 'LogBookCount',
                            flex: 1
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('logbooktype.managelogbooktypes', 'MDC', 'Manage logbooks'),
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
                },
                {
                    xtype: 'grid',
                    store: 'AvailableLogbookTypes',
                    height: 395,
                    selType: 'checkboxmodel',
                    selModel: {
                        checkOnly: true,
                        enableKeyNav: false,
                        showHeaderCheckbox: false
                    },
//                    forceFit: true,
                    columns: {
                        defaults: {
                            sortable: false,
                            menuDisabled: true
                        },
                        items: [
                            {
                                header: Uni.I18n.translate('logbooktype.name', 'MDC', 'Name'),
                                dataIndex: 'name',
                                flex: 3
                            },
                            {
                                xtype: 'obis-column',
                                dataIndex: 'obisCode',
                                flex:2
                            }
                        ]
                    },
                    buttonAlign: 'left',
                    buttons: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            action: 'add',
                            disabled: true,
                            ui: 'action',
                            margin: '0 0 0 -5'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            action: 'cancel',
                            ui: 'link',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#/administration/devicetypes/' + this.up('add-logbook-types').deviceTypeId + '/logbooktypes';
                                    }
                                }
                            }
                        }
                    ]
                },
                {
                    xtype: 'panel',
                    hidden: true,
                    height: 200,
                    items: [
                        {
                            xtype: 'panel',
                            html: "<h3>No logbook types found</h3><br>\
          There are no logbook types. This could be because:<br>\
          &nbsp;&nbsp; - No logbook types have been defined yet.<br>"
                        }
                    ]
                }
            ]
        }
    ]
});
