Ext.define('Mdc.view.setup.comservercomports.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comportEdit',
    required: [
        'Mdc.view.setup.comservercomports.ServletForm',
        'Mdc.view.setup.comservercomports.TCPForm',
        'Mdc.view.setup.comservercomports.UDPForm',
        'Mdc.view.setup.comservercomports.SerialForm',
        'Mdc.view.setup.comservercomports.ComPortPoolsGrid'
    ],
    itemId: 'comPortEdit',
    side: {
        xtype: 'panel',
        ui: 'medium',
        title: Uni.I18n.translate('comserver.title.communicationServers', 'MDC', 'Communication servers'),
        width: 300,
        items: [{
            xtype: 'comserversubmenu',
            itemId: 'comserversubmenu'
        }]
    },
    content: [
        {
            xtype: 'form',
            ui: 'large',
            name: 'addComPortForm',
            itemId: 'addComPortForm',
            title: 'Add inbound communication port',
            buttonAlign: 'left',
            defaults: {
                labelWidth: 250,
                width: 600,
                validateOnChange : false,
                validateOnBlur : false
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
                    fieldLabel: 'Name',
                    required: true,
                    allowBlank: false,
                    name: 'name'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Direction',
                    hidden: true,
                    name: 'direction',
                    renderer: function (value) {
                        return value.charAt(0).toUpperCase() + value.slice(1);
                    }
                },
                {
                    xtype: 'combobox',
                    fieldLabel: 'Type',
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
                    itemId: 'addFormNest'
                }
            ],
            buttons: [
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
                    itemId: 'cancelLink',
                    href: ''
                }
            ]
        }
    ],


    showForm: function (portDirection, portType) {
        var me = this,
            nest = me.down('#addFormNest'),
            form;
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
        return form;
    }
});

