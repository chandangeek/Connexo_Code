/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.endpoint-add',
    itemId: 'endpoint-add',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormInfoMessage',
        'Uni.property.form.Property'
    ],

    initComponent: function(){
        var me = this;
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                layout: 'vbox',
                title: Uni.I18n.translate('endPointAdd.addTitle', 'WSS', 'Add web service endpoint'),
                items: [
                    {
                        xtype: 'form',
                        width: 650,
                        itemId: 'addForm',
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
                                xtype: 'uni-form-error-message',
                                itemId: 'addEndPointFormErrors',
                                name: 'form-errors',
                                margin: '0 0 10 0',
                                hidden: true,
                                width: 750
                            },
                            {
                                xtype: 'uni-form-info-message',
                                itemId: 'addEditWarning',
                                name: 'form-info',
                                margin: '0 0 10 0',
                                text: Uni.I18n.translate('general.formErrorEndPointActive', 'WSS', 'Some attributes can only be changed when the status of the web service endpoint is inactive.'),
                                hidden: true,
                                width: 750
                            },
                            {
                                xtype: 'textfield',
                                name: 'name',
                                itemId: 'endPointName',
                                fieldLabel: Uni.I18n.translate('endPointAdd.name', 'WSS', 'Name'),
                                allowBlank: false,
                                required: true
                            },
                            {
                                xtype: 'combobox',
                                name: 'webServiceName',
                                itemId: 'webServiceCombo',
                                required: true,
                                allowBlank: false,
                                fieldLabel: Uni.I18n.translate('endPointAdd.Webservice', 'WSS', 'Web service'),
                                emptyText: Uni.I18n.translate('general.selectWebservice', 'WSS', 'Select a web service...'),
                                store: 'Wss.store.Webservices',
                                forceSelection: true,
                                displayField: 'name',
                                valueField: 'name',
                                listeners: {
                                    change: function (combobox, newValue, oldValue) {
                                        me.onSelectWebServiceType(combobox, newValue, oldValue);
                                    }
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'grouped-property-form',
                        itemId: 'property-form',
                        width: '100%',
                        defaults: {
                            labelWidth: 250,
                            width: 385,
                            resetButtonHidden: true,
                            hasNotValueSameAsDefaultMessage: true
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'form-buttons',
                        fieldLabel: '&nbsp;',
                        layout: 'hbox',
                        labelWidth: 250,
                        margin: '20 0 0 0',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'btn-add-webservice-endpoint',
                                text: this.action === 'edit'?Uni.I18n.translate('general.save', 'WSS', 'Save'):Uni.I18n.translate('general.add', 'WSS', 'Add'),
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
                ]
            }
        ];
        me.callParent(arguments);
        if(this.action === 'edit'){
            this.editMode();
        }
    },

    editMode: function () {
        this.first = true;
        this.down('panel').setTitle(Ext.String.format(Uni.I18n.translate('endPointAdd.editTitle', 'WSS', 'Edit \'{0}\''),this.record.get('name')));
        this.addFormFields(this.record.get('direction').id, this.record.get('type'));
        var form = this.down('#addForm');
        var values = this.record.data;
        values['logLevel'] = null;
        values['authenticationMethod'] = null;
        form.getForm().setValues(this.record.data);
        form.down('#logLevelCombo').select(this.record.getLogLevel());
        form.down('#storePayloadCombo').select(this.record.getPayloadStrategy());
        form.down('#authenticationCombo').select(this.record.getAuthenticationMethod());
        this.down('#property-form').loadRecord(this.record);
        form.down('#webServiceCombo').disable();
        if (this.record.getAuthenticationMethod().get('id') !== 'none' && Ext.isEmpty(this.record.getGroup())) {
            if (form.down('#userRoleField'))form.down('#userRoleField').select('all');
        } else {
            if (form.down('#userRoleField'))form.down('#userRoleField').select(this.record.getGroup());
        }
        if (this.record.get('active') === true) {
            formErrorsPlaceHolder = form.down('#addEditWarning');
            formErrorsPlaceHolder.show();
            Ext.Array.each(form.getForm().getFields().items, function (field) {
                switch (field.itemId) {
                    case 'logLevelCombo':
                        field.enable();
                        break;
                    case 'traceCheck':
                        field.enable();
                        break;
                    case 'traceFile':
                        field.enable();
                        break;
                    default:
                        field.disable();
                }
            })
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
            if(record){
                this.addFormFields(record.get('direction').id, record.get('type'));
                form.getForm().setValues(values);
                this.down('#property-form').loadRecord(record);
            }
        }
    },

    onSelectAuthenticationType: function(combobox, newValue, oldValue) {
        if(newValue !== null) {
            var value = newValue.localizedValue || newValue;
            value = value.toUpperCase();
            var userRoleField = this.down('#userRoleField'),
                userNameField = this.down('#userNameField'),
                passwordField = this.down('#passwordField'),
                clientIdField = this.down('#clientIdField'),
                clientSecretField = this.down('#clientSecretField');
            if (value === 'NONE') {
                if (!!userRoleField) {
                    userRoleField.setVisible(false)
                }
                if (!!userNameField) {
                    userNameField.setVisible(false)
                }
                if (!!passwordField) {
                    passwordField.setVisible(false)
                }
                if (!!clientIdField) {
                    clientIdField.setVisible(false)
                }
                if (!!clientSecretField) {
                    clientSecretField.setVisible(false)
                }
            } else if (value === 'BASIC_AUTHENTICATION') {
                if (!!userRoleField) {
                    userRoleField.setVisible(true)
                }
                if (!!userNameField) {
                    userNameField.setVisible(true)
                }
                if (!!passwordField) {
                    passwordField.setVisible(true)
                }
                if (!!clientIdField) {
                    clientIdField.setVisible(false)
                }
                if (!!clientSecretField) {
                    clientSecretField.setVisible(false)
                }
            } else if (value === 'OAUTH2_FRAMEWORK'){
                if (!!userRoleField) {
                    userRoleField.setVisible(false)
                }
                if (!!userNameField) {
                    userNameField.setVisible(false)
                }
                if (!!passwordField) {
                    passwordField.setVisible(false)
                }
                if (!!clientIdField) {
                    clientIdField.setVisible(true)
                }
                if (!!clientSecretField) {
                    clientSecretField.setVisible(true)
                }
            }
        }
    },

    addFormFields: function(direction,type){
        var form = this.down('#addForm');
        var me = this;
        Ext.suspendLayouts();
        var remove = form.items.items.slice(4,form.items.items.length);
        Ext.each(remove, function(removeItem){
            form.remove(removeItem);
        });
        form.add(
            {
                xtype: 'textfield',
                itemId: 'url-path',
                name: 'url',
                fieldLabel: direction.toUpperCase()==='INBOUND'?Uni.I18n.translate('endPointAdd.urlPath', 'WSS', 'Url Path'):Uni.I18n.translate('endPointAdd.url', 'WSS', 'Url'),
                required: true,
                allowBlank: false,
                afterBodyEl: [
                    '<div class="x-form-display-field" style="padding-right:10px;color:#686868"><i>',
                    direction.toUpperCase() === 'INBOUND' ? Ext.String.htmlEncode(Uni.I18n.translate('endPointAdd.inboundURLFormatTextPath', 'WSS', 'Define the \'path\' in the URL. It will make the service listen at http://connexo_host:connexo_port/<path>')) : Ext.String.htmlEncode(Uni.I18n.translate('endPointAdd.outboundURLFormatText', 'WSS', 'Define the url which has the format http://<host>:<port>/<path>')),
                    '</i></div>'
                ]
            },
            {
                xtype: 'combobox',
                itemId: 'logLevelCombo',
                name: 'logLevel',
                store: this.logLevelsStore,
                displayField: 'localizedValue',
                forceSelection: true,
                queryMode: 'local',
                valueField: 'id',
                fieldLabel: Uni.I18n.translate('endPointAdd.logLevel', 'WSS', 'Log level'),
                required: true
            },
            {
                xtype: 'combobox',
                itemId: 'storePayloadCombo',
                name: 'payloadStrategy',
                store: this.payloadStrategyStore,
                displayField: 'localizedValue',
                forceSelection: true,
                value: 'ERRORS',
                queryMode: 'local',
                valueField: 'id',
                fieldLabel: Uni.I18n.translate('endPointAdd.storePayload', 'WSS', 'Store request payload'),
                required: true
            },
            {
                xtype: 'checkbox',
                name: 'tracing',
                fieldLabel: Uni.I18n.translate('endPointAdd.traceRequests', 'WSS', 'Trace requests'),
                itemId: 'traceCheck',
                listeners: {
                    change: function (checkbox, newValue, oldValue) {
                        var traceFileField = checkbox.up('form').down('#traceFile');
                        traceFileField.setVisible(newValue);
                        if(newValue === true && traceFileField.getValue() === ''){
                            traceFileField.setValue(checkbox.up('form').down('#endPointName').getValue());
                        }
                    }
                }
            },
            {
                xtype: 'textfield',
                name: 'traceFile',
                itemId: 'traceFile',
                fieldLabel: Uni.I18n.translate('endPointAdd.traceRequestFileName', 'WSS', 'Trace request file name'),
                hidden: true,
                required: true
            },
            {
                xtype: 'checkbox',
                name: 'httpCompression',
                itemId: 'http-compression-id',
                fieldLabel: Uni.I18n.translate('endPointAdd.httpCompression', 'WSS', 'HTTP compression')
            }
        );
        if(type==='SOAP'){
            form.add(
                {
                    xtype: 'checkbox',
                    itemId: 'scheme-validation-id',
                    name: 'schemaValidation',
                    fieldLabel: Uni.I18n.translate('endPointAdd.schemeValidation', 'WSS', 'Scheme validation')
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
                value:'NONE',
                listeners: {
                    change: function (combobox, newValue, oldValue) {
                        me.onSelectAuthenticationType(combobox, newValue, oldValue);
                    }
                }
            }
        );
        if(direction.toUpperCase() ==='INBOUND'){
            form.add(
                {
                    xtype: 'combobox',
                    name: 'group',
                    itemId: 'userRoleField',
                    store: this.rolesStore,
                    value: 'all',
                    fieldLabel: Uni.I18n.translate('endPointAdd.userRole', 'WSS', 'User role'),
                    required: true,
                    displayField: 'name',
                    queryMode: 'local',
                    valueField: 'id',
                    hidden: true
                },
                {
                    xtype: 'textfield',
                    name: 'clientId',
                    itemId: 'clientIdField',
                    fieldLabel: Uni.I18n.translate('endPointAdd.clientId', 'WSS', 'Client ID'),
                    required: true,
                    hidden: true
                },
                {
                    xtype: 'textfield',
                    name: 'clientSecret',
                    itemId: 'clientSecretField',
                    fieldLabel: Uni.I18n.translate('endPointAdd.clientSecret', 'WSS', 'Client Secret'),
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
        Ext.resumeLayouts(true);
    }
});
