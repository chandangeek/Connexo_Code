Ext.define('Mdc.view.setup.comservercomports.forms.TCP', {
    extend: 'Ext.form.Panel',
    alias: 'widget.comPortFormTCP',
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
                    fieldLabel: Uni.I18n.translate('comports.preview.simultaneousConnections', 'MDC', 'Simultaneous connections'),
                    name: 'numberOfSimultaneousConnections'
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
                }
            ]
        }
    ]
});