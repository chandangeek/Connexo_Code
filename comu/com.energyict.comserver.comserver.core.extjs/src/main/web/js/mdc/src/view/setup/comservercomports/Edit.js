Ext.define('Mdc.view.setup.comservercomports.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comportEdit',
    required: [
        'Mdc.view.setup.comservercomports.ServletForm',
        'Mdc.view.setup.comservercomports.TCPForm',
        'Mdc.view.setup.comservercomports.UDPForm',
        'Mdc.view.setup.comservercomports.SerialForm',
        'Mdc.view.setup.comservercomports.ComPortPoolsGrid',
        'Mdc.view.setup.comserver.SideMenu'
    ],
    itemId: 'comPortEdit',

    content: [
        {
            xtype: 'form',
            ui: 'large',
            name: 'addComPortForm',
            itemId: 'addComPortForm',
            title: Uni.I18n.translate('comport.addInboundComPort','MDC','Add inbound communication port'),
            buttonAlign: 'left',
            defaults: {
                labelWidth: 250,
                width: 600,
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'textfield',
                    itemId: 'txt-comport-name',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    required: true,
                    allowBlank: false,
                    name: 'name'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('comServerComPorts.direction', 'MDC', 'Direction'),
                    hidden: true,
                    name: 'direction',
                    renderer: function (value) {
                        return Ext.String.htmlEncode(value.charAt(0).toUpperCase() + value.slice(1));
                    }
                },
                {
                    xtype: 'combobox',
                    fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                    required: true,
                    allowBlank: false,
                    editable: false,
                    name: 'comPortType',
                    itemId: 'comPortTypeSelect',
                    store: 'Mdc.store.ComPortTypes',
                    displayField: 'localizedValue',
                    valueField: 'comPortType',
                    value: 'TCP',
                    queryMode: 'local'
                },
                {
                    xtype: 'container',
                    name: 'addFormNest',
                    itemId: 'addFormNest',
                    margin: '0 0 -20 0'
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'addModel',
                            itemId: 'addEditButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            action: 'cancel',
                            itemId: 'cancelLink'
                        }
                    ]
                }
            ]
        }
    ],

    showForm: function (portDirection, portType) {
        var me = this,
            nest = me.down('#addFormNest'),
            form;
        Ext.suspendLayouts();
        switch (portDirection) {
            case 'inbound' :
                switch (portType) {
                    case 'TCP':
                        nest.removeAll();
                        form = nest.add({xtype: 'tcpForm'});
                        break;
                    case 'UDP':
                        nest.removeAll();
                        form = nest.add({xtype: 'udpForm'});
                        break;
                    case 'SERVLET':
                        nest.removeAll();
                        form = nest.add({xtype: 'servletForm'});
                        break;
                    case 'SERIAL':
                        nest.removeAll();
                        form = nest.add({xtype: 'serialForm'});
                        break;
                }
                form.showInbound();
                break;
            case 'outbound' :
                switch (portType) {
                    case 'TCP':
                        nest.removeAll();
                        form = nest.add({xtype: 'tcpForm'});
                        break;
                    case 'UDP':
                        nest.removeAll();
                        form = nest.add({xtype: 'udpForm'});
                        break;
                    case 'SERVLET':
                        nest.removeAll();
                        form = nest.add({xtype: 'servletForm'});
                        break;
                    case 'SERIAL':
                        nest.removeAll();
                        form = nest.add({xtype: 'serialForm'});
                        break;
                }
                form && form.showOutbound();
                break;
        }
        Ext.resumeLayouts();
        return form;
    },

    initComponent: function () {
        var me = this;
        me.side = {
            xtype: 'panel',
            ui: 'medium',
            width: 300,
            items: [
                {
                    xtype: 'comserversidemenu',
                    itemId: 'comserversidemenu',
                    serverId: me.serverId
                }
            ]
        };
        me.callParent(arguments);
    }
});

