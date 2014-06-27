Ext.define('Mdc.view.setup.device.DeviceCommunicationTopologyPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationTopologyPanel',
    overflowY: 'auto',
    itemId: 'devicecommicationtopologypanel',
    mRID: null,
    ui: 'tile',
    title: Uni.I18n.translate('deviceCommunicationTopology.communicationTopologyTitle', 'MDC', 'Communication topology'),

    items: [
        {
            xtype: 'form',
            itemId: 'deviceCommunicationTopologyForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                labelWidth: 150
            },
            items: [
                {
                    labelAlign: 'right',
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.master', 'MDC', 'Master'),
                    layout: {
                        type: 'vbox'
                    },
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
                    labelAlign: 'right',
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.slave', 'MDC', 'Slave(s)'),
                    layout: {
                        type: 'vbox'
                    },
                    itemId: 'slaveDevicesContainer',
                    items: [
                        {

                        }
                    ]
                }

            ]
        }
    ]
})
;

