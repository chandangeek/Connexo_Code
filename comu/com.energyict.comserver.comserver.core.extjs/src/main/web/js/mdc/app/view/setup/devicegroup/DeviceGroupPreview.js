Ext.define('Mdc.view.setup.devicegroup.DeviceGroupPreview', {
    extend: 'Ext.form.Panel',
    xtype: 'deviceGroupPreview',
    frame: true,
    requires: [
        'Mdc.view.setup.devicegroup.DeviceGroupActionMenu'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'CFG', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'device-group-action-menu'
            }
        }
    ],
    layout: {
        type: 'vbox'
    },
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    items: {
        xtype: 'form',
        border: false,
        itemId: 'deviceGroupPreviewForm',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                xtype: 'container',
                layout: {
                    type: 'column'
                },
                items: [
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
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
                                fieldLabel: Uni.I18n.translate('devicegroup.name', 'MDC', 'Name'),
                                itemId: 'deviceGroupName'

                            },
                            {
                                xtype: 'displayfield',
                                name: 'dynamic',
                                fieldLabel: Uni.I18n.translate('devicegroup.type', 'MDC', 'Type'),
                                renderer: function (value) {
                                    if (value) {
                                        return Uni.I18n.translate('devicegroup.dynamic', 'MDC', 'Dynamic')
                                    } else {
                                        return Uni.I18n.translate('devicegroup.static', 'MDC', 'Static')
                                    }
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                columnWidth: 0.5,
                                fieldLabel: Uni.I18n.translate('deviceGroup.searchCriteria', 'MDC', 'Search criteria'),
                                labelAlign: 'right',
                                layout: {
                                    type: 'vbox'
                                },
                                itemId: 'searchCriteriaContainer',
                                items: [

                                ]
                            }
                        ]
                    }

                ]
            }
        ]
    },
    // todo: set empty text
    emptyText: '<h3>' + Uni.I18n.translate('devicegroup.noDeviceGroupSelected', 'MDC', 'No device group selected') + '</h3><p>' + Uni.I18n.translate('devicegroup.selectDeviceGroup', 'MDC', 'Select a device group to see its details') + '</p>'
});
