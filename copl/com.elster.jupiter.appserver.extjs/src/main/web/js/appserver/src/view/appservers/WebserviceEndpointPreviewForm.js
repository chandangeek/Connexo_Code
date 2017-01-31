/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.WebserviceEndpointPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.webservice-preview-form',
    router: null,

    layout: {
        type: 'vbox'
    },
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.webservice', 'APR', 'Web service'),
                name: 'webServiceName',
                renderer: function(value) {
                    var record = this.up().getRecord();
                    if(record) {
                        if (value && record.get('available')) {
                            return value;
                        } else if (value && !record.get('available')) {
                            return value + ' (' + Uni.I18n.translate('general.notAvailable', 'APR', 'not available') + ')' + '<span class="icon-warning" style="margin-left:5px; color:#eb5642;"></span>';
                        }
                    }
                    return '-';
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.url', 'APR', 'Url'),
                name: 'previewUrl'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.logLevel', 'APR', 'Log level'),
                name: 'logLevel',
                renderer: function (value) {
                    return value.localizedValue;
                }

            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('webservices.traceRequests', 'APR', 'Trace requests'),
                name: 'tracing',
                renderer: me.renderYesOrNo
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('webservices.traceRequestsFileName', 'APR', 'Trace requests file name'),
                name: 'traceFile',
                renderer: function(value,field){
                    if(field.up('form').down('[name=tracing]').getValue()===''){
                        this.hide();
                    } else {
                        this.show();
                    }
                    return value;
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('webservices.httpCompression', 'APR', 'HTTP compression'),
                name: 'httpCompression',
                renderer: me.renderYesOrNo
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('webservices.schemeValidation', 'APR', 'Scheme validation'),
                name: 'schemaValidation',
                renderer: function (value, field) {
                    var rec = field.up('form').getRecord();
                    if (rec) {
                        rec.get('type') === 'REST' ? this.hide() : this.show();
                        if (value === true) {
                            return Uni.I18n.translate('general.yes', 'APR', 'Yes');
                        } else {
                            return Uni.I18n.translate('general.no', 'APR', 'No')
                        }
                    }
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('webservices.authenticationRequired', 'APR', 'Authentication'),
                name: 'authenticationMethod',
                renderer: function(value){
                    return !Ext.isEmpty(value)?value.localizedValue:'';
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.userRole', 'APR', 'User role'),
                name: 'group',
                renderer: function(value,field){
                    if(field.up('form').down('[name=authenticationMethod]').getValue().id === "BASIC_AUTHENTICATION" && Ext.isEmpty(value)){
                        this.show();
                        return Uni.I18n.translate('endPointAdd.all', 'APR', 'All');
                    }
                    else if(Ext.isEmpty(value)){
                        this.hide();
                        return value;
                    } else {
                        this.show();
                        return value.name;
                    }
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.userName', 'APR', 'Username'),
                name: 'username',
                renderer: function(value){

                    Ext.isEmpty(value)?this.hide():this.show();
                    return value;
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.password', 'APR', 'Password'),
                name: 'password',
                renderer: function(value){
                    Ext.isEmpty(value)?this.hide():this.show();
                    return value;
                }
            }
        ];
        me.callParent(arguments);
    },

    updateWebservicePreview: function (record) {
        var me = this;

        if (!Ext.isDefined(record)) {
            return;
        }
        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.loadRecord(record);

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },

    renderYesOrNo: function (value) {
        if (value === true) {
            return Uni.I18n.translate('general.yes', 'APR', 'Yes');
        } else {
            return Uni.I18n.translate('general.no', 'APR', 'No')
        }
    }
});