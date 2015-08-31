Ext.define('Mdc.view.setup.comservercomports.UDPForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.udpForm',
    defaults: {
        labelWidth: 250,
        width: 600,
        validateOnChange : false,
        validateOnBlur : false
    },
    items: [
        {
            xtype: 'numberfield',
            itemId: 'num-udp-simultaneous-connections',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.connectionCount', 'MDC', 'Simultaneous connections'),
            required: true,
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
            allowBlank: false,
            name: 'numberOfSimultaneousConnections',
            value: 1,
            width: 370
        },
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
            hidden: true,
            minValue: 0,
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
            allowBlank: false,
            name: 'portNumber',
            value: 0,
            width: 370
        },
        {
            xtype: 'numberfield',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.bufSize', 'MDC', 'Buffer size'),
            required: true,
            allowBlank: false,
            name: 'bufferSize',
            minValue: 1,
            stripCharsRe: /\D/,
            listeners: {
                blur: {
                    fn: function(field){
                        if(Ext.isEmpty(field.getValue())) {
                            field.setValue(1024);
                        }
                    }
                }
            },
            value: 1024,
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
            emptyText: Uni.I18n.translate('comports.selectInboundPool','MDC','Select inbound communication port pool...')
        },
        {
            xtype: 'fieldcontainer',
            itemId: 'helpLabel',
            fieldLabel: '&nbsp;',
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
        this.down('numberfield[name=bufferSize]').show();
        this.down('#inboundPool').show();
        this.down('#comportpoolid').hide();
        this.down('#comportpoolid').disable();
        this.down('#helpLabel').show();
    },

    showOutbound: function(){
        this.down('numberfield[name=portNumber]').hide();
        this.down('numberfield[name=portNumber]').disable();

        this.down('numberfield[name=bufferSize]').hide();
        this.down('numberfield[name=bufferSize]').disable();
        this.down('#inboundPool').hide();
        this.down('#inboundPool').disable();
        this.down('#comportpoolid').show();
        this.down('#helpLabel').hide();
    }
});
