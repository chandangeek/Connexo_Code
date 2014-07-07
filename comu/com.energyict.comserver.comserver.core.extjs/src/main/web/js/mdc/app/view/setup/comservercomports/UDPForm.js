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
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.connectionCount', 'MDC', 'Simultaneous connections'),
            required: true,
            minValue: 1,
            allowBlank: false,
            name: 'numberOfSimultaneousConnections',
            value: 1,
            width: 350
        },
        {
            xtype: 'fieldcontainer',
            required: true,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.outPools', 'MDC', 'Outbound comport pools'),
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
            minValue: 1,
            allowBlank: false,
            name: 'portNumber',
            value: 1,
            width: 350
        },
        {
            xtype: 'numberfield',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.bufSize', 'MDC', 'Buffer size'),
            required: true,
            allowBlank: false,
            name: 'bufferSize',
            value: 1024,
            width: 350
        },
        {
            xtype: 'combobox',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.inPools', 'MDC', 'Inbound communication port pool'),
            required: true,
            allowBlank: false,
            editable: false,
            name: 'inboundPool',
            emptyText: 'Select inbound communication pool'
        }
    ],
    showInbound: function(){
        this.down('numberfield[name=portNumber]').show();
        this.down('numberfield[name=bufferSize]').show();
        this.down('combobox[name=inboundPool]').show();
        this.down('#comportpoolid').hide();
        this.down('#comportpoolid').disable();
    },

    showOutbound: function(){
        this.down('numberfield[name=portNumber]').hide();
        this.down('numberfield[name=portNumber]').disable();

        this.down('numberfield[name=bufferSize]').hide();
        this.down('numberfield[name=bufferSize]').disable();
        this.down('combobox[name=inboundPool]').hide();
        this.down('combobox[name=inboundPool]').disable();
        this.down('#comportpoolid').show();
    }
});
