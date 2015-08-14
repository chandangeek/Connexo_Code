Ext.define('Mdc.view.setup.comservercomports.TCPForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.tcpForm',
    defaults: {
        labelWidth: 250,
        width: 600,
        validateOnChange : false,
        validateOnBlur : false
    },
    items: [
        {
            xtype: 'numberfield',
            itemId: 'num-tcp-simultaneous-connections',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.connectionCount', 'MDC', 'Simultaneous connections'),
            required: true,
            allowBlank: false,
            minValue: 1,
            stripCharsRe: /\D/,
            listeners: {
                blur: {
                    fn: function(field){
                        if(Ext.isEmpty(field.getValue())) {
                            field.setValue(1);
                        }
                    }
                }
            },
            name: 'numberOfSimultaneousConnections',
            value: 1,
            width: 370                                    },
        {
            xtype: 'fieldcontainer',
            required: false,
            fieldLabel: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
            itemId: 'comportpoolid',
            items: [
                {
                    xtype: 'outboundportcomportpools'
                }
            ]
        },
        {
            xtype: 'numberfield',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.portNum', 'MDC', 'Port number'),
            required: true,
            allowBlank: false,
            minValue: 1,
            stripCharsRe: /\D/,
            listeners: {
                blur: {
                    fn: function(field){
                        if(Ext.isEmpty(field.getValue())) {
                            field.setValue(1);
                        }
                    }
                }
            },
            name: 'portNumber',
            value: 0,
            width: 370
        },
        {
            xtype: 'combobox',
            fieldLabel: Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool'),
            required: false,
            store: 'Mdc.store.InboundComPortPools',
            editable: false,
            queryMode: 'local',
            itemId: 'inboundPool',
            name: 'comPortPool_id',
            displayField: 'name',
            valueField: 'id',
            emptyText: 'Select inbound communication port pool...'
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: '&nbsp;',
            name: 'helpLabelInboundComPorts',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    html: '<span style="color: grey"><i>' + Uni.I18n.translate('comports.preview.noInboundCommPortPool', 'MDC', 'When no inbound communication port pool is selected,<br> the port cannot be activated') + '</i></span>',
                    xtype: 'component'

                }
            ]
        }
    ],


    showInbound: function(){
        this.down('numberfield[name=portNumber]').show();
        this.down('combobox[name=comPortPool_id]').show();
        this.down('fieldcontainer[name=helpLabelInboundComPorts]').show();

        this.down('#comportpoolid').hide();
        this.down('#comportpoolid').disable();

    },

    showOutbound: function(){
        this.down('numberfield[name=portNumber]').hide();
        this.down('numberfield[name=portNumber]').disable();
        this.down('fieldcontainer[name=helpLabelInboundComPorts]').hide();
        this.down('combobox[name=comPortPool_id]').hide();
        this.down('combobox[name=comPortPool_id]').disable();

        this.down('#comportpoolid').show();

    }
});