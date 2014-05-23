Ext.define('Mdc.view.setup.device.DeviceGeneralInformationPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceGeneralInformationPanel',
    overflowY: 'auto',
    itemId: 'devicegeneralinformationpanel',
    title: Uni.I18n.translate('deviceGeneralInformation.generalInformationTitle', 'MDC', 'General information'),
    ui: 'tile',
    deviceId: null,
    items: [
        {
            xtype: 'form',
            itemId: 'deviceGeneralInformationForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                labelWidth: 150,
                xtype: 'displayfield'
            },
            items: [
                {
                    name: 'mRID',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.mrid', 'MDC', 'MRID')
                },
                {
                    name: 'serialNumber',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.serialNumber', 'MDC', 'Serial number')
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deviceType', 'MDC', 'Device type'),
                    layout: {
                        type: 'vbox'
                    },
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
                    name: 'yearOfCertification',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.yearOfCertification', 'MDC', 'Year of certification')
                },
                {
                    name: 'batch',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.batch', 'MDC', 'Batch')
                }
            ]
        }
    ]
})
;

