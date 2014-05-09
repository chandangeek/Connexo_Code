Ext.define('Mdc.view.setup.device.DeviceGeneralInformationPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceGeneralInformationPanel',
    overflowY: 'auto',
    itemId: 'devicegeneralinformationpanel',
    deviceId: null,
    margin: '0 10 10 10',
    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'component',
                html: '<h4>' + Uni.I18n.translate('deviceGeneralInformation.generalInformationTitle', 'MDC', 'General information') + '</h4>',
                itemId: 'generalInformationTitle'
            },

            {
                xtype: 'form',
                itemId: 'deviceGeneralInformationForm',
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
                        xtype: 'displayfield',
                        name: 'mRID',
                        fieldLabel: Uni.I18n.translate('deviceGeneralInformation.mrid', 'MDC', 'MRID'),
                        labelAlign: 'right'
                    },
                    {
                        xtype: 'displayfield',
                        name: 'serialNumber',
                        fieldLabel: Uni.I18n.translate('deviceGeneralInformation.serialNumber', 'MDC', 'Serial number'),
                        labelAlign: 'right'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deviceType', 'MDC', 'Device type'),
                        layout: {
                            type: 'vbox'
                        },
                        margin: '0 0 10 0',
                        items: [
                            {
                                xtype: 'component',
                                name: 'deviceTypeName',
                                cls: 'x-form-display-field',
                                autoEl: {
                                    tag: 'a',
                                    href: '#',
                                    html: Uni.I18n.translate('deviceGeneralInformation.deviceType', 'MDC', 'Device type')
                                },
                                itemId: 'deviceGeneralInformationDeviceTypeLink'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deviceConfiguration', 'MDC', 'Device configuration'),
                        layout: {
                            type: 'vbox'
                        },
                        margin: '0 0 10 0',
                        items: [
                            {
                                xtype: 'component',
                                name: 'deviceConfigurationName',
                                cls: 'x-form-display-field',
                                autoEl: {
                                    tag: 'a',
                                    href: '#',
                                    html: Uni.I18n.translate('deviceGeneralInformation.deviceConfiguration', 'MDC', 'Device configuration')
                                },
                                itemId: 'deviceGeneralInformationDeviceConfigurationLink'
                            }
                        ]
                    },
                    {
                        xtype: 'displayfield',
                        name: 'yearOfCertification',
                        fieldLabel: Uni.I18n.translate('deviceGeneralInformation.yearOfCertification', 'MDC', 'Year of certification'),
                        labelAlign: 'right'
                    },
                    {
                        xtype: 'displayfield',
                        name: 'batch',
                        fieldLabel: Uni.I18n.translate('deviceGeneralInformation.batch', 'MDC', 'Batch'),
                        labelAlign: 'right'
                    }
                ]
            }
        ];
        this.callParent();
    }
})
;

