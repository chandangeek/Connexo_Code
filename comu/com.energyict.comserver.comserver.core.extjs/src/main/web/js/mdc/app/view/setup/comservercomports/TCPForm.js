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
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.connectionCount', 'MDC', 'Simultaneous connections'),
            required: true,
            allowBlank: false,
            minValue: 1,
            name: 'numberOfSimultaneousConnections',
            value: 1,
            width: 350
        },
        {
            xtype: 'fieldcontainer',
            required: false,
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
            allowBlank: false,
            minValue: 1,
            name: 'portNumber',
            value: 0,
            width: 350
        },
        {
            xtype: 'combobox',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.inPools', 'MDC', 'Communication port pool'),
            required: false,
            store: 'Mdc.store.InboundComPortPools',
            editable: false,
            queryMode: 'local',
            itemId: 'inboundPool',
            name: 'comPortPool_id',
            displayField: 'name',
            valueField: 'id',
            emptyText: 'Select inbound communication pool'
        }
    ],


    showInbound: function(){
        this.down('numberfield[name=portNumber]').show();
        this.down('combobox[name=comPortPool_id]').show();

        this.down('#comportpoolid').hide();
        this.down('#comportpoolid').disable();

    },

    showOutbound: function(){
        this.down('numberfield[name=portNumber]').hide();
        this.down('numberfield[name=portNumber]').disable();

        this.down('combobox[name=comPortPool_id]').hide();
        this.down('combobox[name=comPortPool_id]').disable();

        this.down('#comportpoolid').show();

    }
});