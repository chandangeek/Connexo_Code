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
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    name: 'name'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.comServer', 'MDC', 'Communication server'),
                    name: 'comServerName'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.direction', 'MDC', 'Direction'),
                    name: 'direction'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.globalModemInitialization', 'MDC', 'Global modem initialization'),
                    name: 'globalModemInitStrings'
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
                    fieldLabel: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
                    name: 'outboundComPortPoolIds',
                    htmlEncode: false
                },
                {
                    fieldLabel: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
                    name: 'inboundComPortPools',
                    htmlEncode: false
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
                    name: 'atCommandTimeout'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.AtCommandTry', 'MDC', 'AT command try'),
                    name: 'atCommandTry'
                }
            ]
        }
    ],

    showData: function (direction) {
        var isInbound = direction === 'Inbound';

        this.down('displayfield[name=globalModemInitStrings]').setVisible(isInbound);
        this.down('displayfield[name=modemInitStrings]').setVisible(isInbound);
        this.down('displayfield[name=addressSelector]').setVisible(isInbound);
        this.down('displayfield[name=serialPortConf]').setVisible(isInbound);
        this.down('displayfield[name=ringCount]').setVisible(isInbound);
        this.down('displayfield[name=maximumNumberOfDialErrors]').setVisible(isInbound);
        this.down('displayfield[name=connectTimeout]').setVisible(isInbound);
        this.down('displayfield[name=delayAfterConnect]').setVisible(isInbound);
        this.down('displayfield[name=delayBeforeSend]').setVisible(isInbound);
        this.down('displayfield[name=atCommandTimeout]').setVisible(isInbound);
        this.down('displayfield[name=atCommandTry]').setVisible(isInbound);
    }
});