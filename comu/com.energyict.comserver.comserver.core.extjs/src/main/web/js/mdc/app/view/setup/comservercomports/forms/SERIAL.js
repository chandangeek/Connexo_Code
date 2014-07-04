Ext.define('Mdc.view.setup.comservercomports.forms.SERIAL', {
    extend: 'Ext.form.Panel',
    alias: 'widget.comPortFormSERIAL',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    ui: 'medium',

    items: [
        {
            defaults: {
                xtype: 'displayfield',
                labelWidth: 300
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.formFieldLabel.name', 'MDC', 'Name'),
                    name: 'name'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.direction', 'MDC', 'Direction'),
                    name: 'direction'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.modemInitialization', 'MDC', 'Modem initialization'),
                    name: 'modemInitStrings'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.addressSelector', 'MDC', 'Address selector'),
                    name: 'addressSelector'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.serialPortConfiguration', 'MDC', 'Serial port configuration'),
                    name: 'serialPortConf'
                }
            ]
        },
        {
            defaults: {
                xtype: 'displayfield',
                labelWidth: 300
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                    name: 'status'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                    name: 'comPortType'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.communicationPortPools', 'MDC', 'Communication port pools'),
                    name: 'inboundComPortPools'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.ringCount', 'MDC', 'Ring count'),
                    name: 'ringCount'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.maxNumberOfDialErrors', 'MDC', 'Max. number of dial errors'),
                    name: 'maximumNumberOfDialErrors'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.connectTimeout', 'MDC', 'Connect timeout'),
                    name: 'connectTimeout'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.delayAfterConnect', 'MDC', 'Delay after connect'),
                    name: 'delayAfterConnect'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.delayBeforeSend', 'MDC', 'Delay before send'),
                    name: 'delayBeforeSend'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.AtCommandTimeout', 'MDC', 'AT command timeout'),
                    name: 'atCommandTimeout',
                    renderer: function (val) {
                        return val ? val.count ? val.count + (val.timeUnit ? ' ' + val.timeUnit : '') : '' : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.AtCommandTry', 'MDC', 'AT command try'),
                    name: 'atCommandTry'
                }
            ]
        }
    ]
});