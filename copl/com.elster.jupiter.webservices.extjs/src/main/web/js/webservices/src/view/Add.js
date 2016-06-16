Ext.define('Wss.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.endpoint-add',
    itemId: 'endpoint-add',
    requires: [

    ],

    initComponent: function(){
        var me = this;
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                layout: 'vbox',
                title: Uni.I18n.translate('endPointAdd.title', 'WSS', 'Add webservice endpoint'),
                items: [
                    {
                        xtype: 'form',
                        width: 650,
                        itemId: 'addForm',
                        //hydrator: 'Uni.util.Hydrator',
                        buttonAlign: 'left',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                name: 'errors',
                                ui: 'form-error-framed',
                                itemId: 'addEndPointFormErrors',
                                layout: 'hbox',
                                margin: '0 0 10 0',
                                hidden: true,
                                defaults: {
                                    xtype: 'container',
                                    margin: '0 0 0 10'
                                }
                            },
                            {
                                xtype: 'textfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('endPointAdd.name', 'WSS', 'Name'),
                                required: true
                            },
                            {
                                xtype: 'combobox',
                                name: 'webServiceName',
                                // itemId: 'deviceAddSerial',
                                required: true,
                                fieldLabel: Uni.I18n.translate('endPointAdd.Webservice', 'WSS', 'Webservice'),
                                store: 'Wss.store.Webservices',
                                displayField: 'name',
                                valueField: 'name',
                                listeners: {
                                    change: function (combobox, newValue, oldValue) {
                                        me.onSelectWebServiceType(combobox, newValue, oldValue);
                                    }
                                }
                            }

                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    onSelectWebServiceType: function(combobox, newValue, oldValue){
        var me = this;
        var record = combobox.findRecordByValue(newValue);
        var form = this.down('#addForm');
        Ext.suspendLayouts();
        var remove = form.items.items.slice(3,form.items.items.length);
        Ext.each(remove, function(removeItem){
            form.remove(removeItem);
        });

        form.add(
            {
                xtype: 'textfield',
                name: 'url',
                fieldLabel: record.get('direction')==='Inbound'?Uni.I18n.translate('endPointAdd.urlPath', 'WSS', 'Url Path'):Uni.I18n.translate('endPointAdd.url', 'WSS', 'Url'),
                required: true
            },
            {
                xtype: 'combobox',
                itemId: 'logLevelCombo',
                name: 'logLevel',
                store: 'Wss.store.LogLevels',
                displayField: 'localizedValue',
                valueField: 'localizedValue',
                fieldLabel: Uni.I18n.translate('endPointAdd.logLevel', 'WSS', 'Log level'),
                required: true,
            },
            {
                xtype: 'checkbox',
                name: 'tracing',
                fieldLabel: Uni.I18n.translate('endPointAdd.traceRequests', 'WSS', 'Trace requests'),
                required: true
            },
            {
                xtype: 'textfield',
                name: 'tracingFileName',
                fieldLabel: Uni.I18n.translate('endPointAdd.traceRequestFileName', 'WSS', 'Trace request file name'),
                required: true
            },
            {
                xtype: 'checkbox',
                name: 'httpCompression',
                fieldLabel: Uni.I18n.translate('endPointAdd.httpCompression', 'WSS', 'HTTP compression'),
                required: true
            }
        );
        if(record.get('type')==='SOAP'){
            form.add(
                {
                    xtype: 'checkbox',
                    name: 'schemaValidation',
                    fieldLabel: Uni.I18n.translate('endPointAdd.schemeValidation', 'WSS', 'Scheme validation'),
                    required: true
                }
            )
        }
        form.add(
            {
                xtype: 'combobox',
                name: 'authenticated',
                fieldLabel: Uni.I18n.translate('endPointAdd.authentication', 'WSS', 'Authentication'),
                required: true
            }
        );
        if(record.get('direction').id ==='INBOUND'){
            form.add(
                {
                    xtype: 'combobox',
                    name: 'userRole',
                    fieldLabel: Uni.I18n.translate('endPointAdd.userRole', 'WSS', 'User role'),
                    required: true
                }
            );
        } else if (record.get('direction').id ==='OUTBOUND') {
            form.add(
                {
                    xtype: 'textfield',
                    name: 'username',
                    fieldLabel: Uni.I18n.translate('endPointAdd.userName', 'WSS', 'User name'),
                    required: true
                },
                {
                    xtype: 'textfield',
                    name: 'password',
                    fieldLabel: Uni.I18n.translate('endPointAdd.password', 'WSS', 'Password'),
                    required: true
                }
            );
        }
        form.add(
            {
                xtype: 'fieldcontainer',
                itemId: 'form-buttons',
                fieldLabel: '&nbsp;',
                layout: 'hbox',
                margin: '20 0 0 0',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btn-add-webservice-endpoint',
                        text: Uni.I18n.translate('general.add', 'WSS', 'Add'),
                        ui: 'action',
                        action: me.action
                    },
                    {
                        xtype: 'button',
                        itemId: 'btn-cancel-webservice-endpoint',
                        text: Uni.I18n.translate('general.cancel', 'WSS', 'Cancel'),
                        ui: 'link',
                        action: 'cancel',
                        href: me.returnLink
                    }
                ]
            }
        )
        Ext.resumeLayouts(true);
    }
});
