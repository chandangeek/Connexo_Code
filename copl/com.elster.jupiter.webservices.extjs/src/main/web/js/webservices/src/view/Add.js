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
                                itemId: 'webServiceCombo',
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
        if(this.action === 'edit'){
            this.first = true;
            this.addFormFields(this.record.get('direction').id,this.record.get('type'));
            debugger;
            var form = this.down('#addForm');
         //   form.loadRecord(this.record);
            var values= this.record.data;
            values['logLevel'] = null;
            values['authenticationMethod'] = null;
            form.getForm().setValues(this.record.data);
            form.down('#logLevelCombo').select(this.record.getLogLevel());
            form.down('#authenticationCombo').select(this.record.getAuthenticationMethod());
            //form.getForm().setValues({
            //    logLevel: this.record.getLogLevel(),
            //    authenticationMethod: this.record.getAuthenticationMethod()
            //});
        }
    },

    onSelectWebServiceType: function(combobox, newValue, oldValue){
        var me = this;
        if(this.action === 'edit' && this.first === true){
           this.first = false;
        } else {
            var form = this.down('#addForm');
            var values = form.getValues();
            values.webServiceName = newValue;
            var record = combobox.findRecordByValue(newValue);
            this.addFormFields(record.get('direction').id, record.get('type'));
            debugger;
            form.getForm().setValues(values);
        }
    },

    onSelectAuthenticationType: function(combobox, newValue, oldValue) {
        var value = newValue.localizedValue || newValue;
        value = value.toUpperCase();
       // var record = combobox.findRecordByValue(value),
        var userRoleField = this.down('#userRoleField'),
            userNameField = this.down('#userNameField'),
            passwordField = this.down('#passwordField');
       // if(record.get('authenticationMethod')==='NONE'){
        if(value==='NONE'){
            if(!!userRoleField){userRoleField.setVisible(false)}
            if(!!userNameField){userNameField.setVisible(false)}
            if(!!passwordField){passwordField.setVisible(false)}
        //} else if (record.get('authenticationMethod')==='BASIC_AUTHENTICATION'){
        } else if (value ==='BASIC_AUTHENTICATION'){
            if(!!userRoleField){userRoleField.setVisible(true)}
            if(!!userNameField){userNameField.setVisible(true)}
            if(!!passwordField){passwordField.setVisible(true)}
        }
    },

    addFormFields: function(direction,type){
        var form = this.down('#addForm');
        var me = this;
        Ext.suspendLayouts();
        var remove = form.items.items.slice(3,form.items.items.length);
        Ext.each(remove, function(removeItem){
            form.remove(removeItem);
        });
        form.add(
            {
                xtype: 'textfield',
                name: 'url',
                fieldLabel: direction.toUpperCase()==='INBOUND'?Uni.I18n.translate('endPointAdd.urlPath', 'WSS', 'Url Path'):Uni.I18n.translate('endPointAdd.url', 'WSS', 'Url'),
                required: true
            },
            {
                xtype: 'combobox',
                itemId: 'logLevelCombo',
                name: 'logLevel',
                store: this.logLevelsStore,
                displayField: 'localizedValue',
                queryMode: 'local',
                valueField: 'id',
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
                name: 'traceFile',
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
        if(type==='SOAP'){
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
                name: 'authenticationMethod',
                itemId: 'authenticationCombo',
                fieldLabel: Uni.I18n.translate('endPointAdd.authentication', 'WSS', 'Authentication'),
                required: true,
                store: this.authenticationMethodStore,
                queryMode: 'local',
                displayField: 'localizedValue',
                valueField: 'id',
                listeners: {
                    change: function (combobox, newValue, oldValue) {
                        me.onSelectAuthenticationType(combobox, newValue, oldValue);
                    }
                }
            }
        );
        debugger;
        if(direction.toUpperCase() ==='INBOUND'){
            form.add(
                {
                    xtype: 'combobox',
                    name: 'userRole',
                    itemId: 'userRoleField',
                    fieldLabel: Uni.I18n.translate('endPointAdd.userRole', 'WSS', 'User role'),
                    required: true,
                    hidden: true
                }
            );
        } else if (direction.toUpperCase() ==='OUTBOUND') {
            form.add(
                {
                    xtype: 'textfield',
                    name: 'username',
                    itemId: 'userNameField',
                    fieldLabel: Uni.I18n.translate('endPointAdd.userName', 'WSS', 'User name'),
                    required: true,
                    hidden: true
                },
                {
                    xtype: 'textfield',
                    name: 'password',
                    itemId: 'passwordField',
                    fieldLabel: Uni.I18n.translate('endPointAdd.password', 'WSS', 'Password'),
                    required: true,
                    hidden: true
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
        );
        Ext.resumeLayouts(true);
    }
});
