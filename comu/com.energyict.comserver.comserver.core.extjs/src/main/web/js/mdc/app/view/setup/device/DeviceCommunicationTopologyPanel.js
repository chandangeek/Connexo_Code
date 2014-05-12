Ext.define('Mdc.view.setup.device.DeviceCommunicationTopologyPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationTopologyPanel',
    overflowY: 'auto',
    itemId: 'devicecommicationtopologypanel',
    deviceId: null,
    margin: '0 10 10 10',

    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'component',
                html: '<h4>' + Uni.I18n.translate('deviceCommunicationTopology.communicationTopologyTitle', 'MDC', 'Communication topology') + '</h4>',
                itemId: 'communicationTopologyTitle'
            },
            {
                xtype: 'form',
                itemId: 'deviceCommunicationTopologyForm',
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.master', 'MDC', 'Master'),
                        layout: {
                            type: 'vbox'
                        },
                        margin: '0 0 10 0',
                        items: [
                            {
                                xtype: 'component',
                                cls: 'x-form-display-field',
                                autoEl: {
                                    tag: 'a',
                                    href: '#',
                                    html: ''
                                },
                                itemId: 'deviceCommunicationtopologyMasterLink'
                            }

                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.slave', 'MDC', 'Slave(s)'),
                        layout: {
                            type: 'vbox'
                        },
                        margin: '0 0 10 0',
                        itemId: 'slaveDevicesContainer',
                        items: [
                            {

                            }
                        ]
                    }

                ]
            }
        ];
        this.callParent();
    }
})
;

