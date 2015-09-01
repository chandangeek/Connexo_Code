Ext.define('Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceConnectionMethodPreview',
    itemId: 'deviceConnectionMethodPreview',
    requires: [
        'Mdc.model.DeviceConnectionMethod',
        'Mdc.view.setup.property.PropertyView',
        'Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodActionMenu',
        'Mdc.util.ScheduleToStringConverter'
    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: Uni.I18n.translate('general.details','MDC','Details'),

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'device-connection-method-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'panel',
            border: false,
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + Uni.I18n.translate('deviceconnectionmethod.noConnectionMethodSelected', 'MDC', 'No connection method selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('deviceconnectionmethod.details.selectConnectionMethod', 'MDC', 'Select a connection method to see its details') + '</h5>'
                }
            ]

        },
        {
            xtype: 'form',
            border: false,
            itemId: 'deviceConnectionMethodPreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
//                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'container',
                            columnWidth: 0.49,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                    itemId: 'deviceName'

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'direction',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionmethod.direction', 'MDC', 'Direction')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'allowSimultaneousConnections',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionmethod.simultaneousConnectionsAllowed', 'MDC', 'Simultaneous connections allowed'),
                                    renderer: function (value) {
                                        return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');

                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'isDefault',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionmethod.default', 'MDC', 'Default'),
                                    renderer: function (value) {
                                        return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'status',
                                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                                    dataIndex: 'status',
                                    renderer: function (value, b, record) {
                                        switch (value) {
                                            case 'connectionTaskStatusIncomplete':
                                                return Uni.I18n.translate('deviceconnectionmethod.status.incomplete', 'MDC', 'Incomplete');
                                            case 'connectionTaskStatusActive':
                                                return Uni.I18n.translate('general.active', 'MDC', 'Active');
                                            case 'connectionTaskStatusInActive':
                                                return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                                            default :
                                                return '';
                                        }
                                    }
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            columnWidth: 0.49,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionType',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionmethod.connectionType', 'MDC', 'Connection type')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'comPortPool',
                                    fieldLabel: Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionStrategy',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionmethod.connectionStrategy', 'MDC', 'Connection strategy'),
                                    renderer: function (value) {
                                        if (value) {
                                            var connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');
                                            return connectionStrategiesStore.findRecord('connectionStrategy', value).get('localizedValue');
                                        } else {
                                            return Ext.String.htmlEncode(value) || Uni.I18n.translate('general.undefined', 'MDC', 'Undefined');
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'nextExecutionSpecs',
                                    fieldLabel: Uni.I18n.translate('communicationschedule.schedule', 'MDC', 'Schedule'),
                                    renderer: function (value) {
                                        return Mdc.util.ScheduleToStringConverter.convert(value) || Uni.I18n.translate('general.undefined', 'MDC', 'Undefined');
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionWindow',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.connectionWindow', 'MDC', 'Connection window'),
                                    renderer: function(value) {
                                        if (value) {
                                            if (value.start || value.end) {
                                                var startMinutes = (value.start/3600 | 0),
                                                    startSeconds = (value.start/60 - startMinutes*60),
                                                    endMinutes = (value.end/3600 | 0),
                                                    endSeconds = (value.end/60 - endMinutes*60);

                                                var addZeroIfOneSymbol = function (timeCount) {
                                                    var timeInString = timeCount.toString();

                                                    if (timeInString.length === 1) {
                                                        timeInString = '0' + timeInString;
                                                    }
                                                    return timeInString;
                                                };

                                                return Uni.I18n.translate('connectionmethod.between', 'MDC', 'Between') + ' ' + addZeroIfOneSymbol(startMinutes) + ':' + addZeroIfOneSymbol(startSeconds)  + ' ' + Uni.I18n.translate('general.and', 'MDC', 'And').toLowerCase() + ' ' + addZeroIfOneSymbol(endMinutes) + ':' + addZeroIfOneSymbol(endSeconds);

                                            } else {
                                                return Uni.I18n.translate('connectionmethod.norestriction', 'MDC', 'No restrictions');
                                            }
                                        }
                                    }
                                }
                            ]
                        }


                    ]
                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'connectionDetailsTitle',
                    hidden: true,
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    defaults: {
                        labelWidth: 250
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: '<h3>' + Uni.I18n.translate('deviceconnectionmethod.connectionDetails', 'MDC', 'Connection details') + '</h3>',
                            text: ''
                        }
                    ]
                },
                {
                    xtype: 'property-form',
                    itemId: 'propertyForm',
                    isEdit: false,
                    layout: 'column',

                    defaults: {
                        xtype: 'container',
                        layout: 'form',
                        resetButtonHidden: true,
                        labelWidth: 250,
                        columnWidth: 0.5
                    }
                }
//                {
//                    xtype: 'propertyView'
//                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});


