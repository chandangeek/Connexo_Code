Ext.define('Wss.view.PreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.webservices-preview-form',
    layout: {
        type: 'column'
    },
    isLandingPage: false,

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            defaults: {
                labelWidth: 250
            },
            items: [
                {
                    xtype: 'displayfield',
                    fieldLabel:Uni.I18n.translate('general.status', 'WSS', 'Status'),
                    name: 'active',
                    hidden: !me.isLandingPage,
                    renderer: function(value) {
                        if(value === true) {
                            return Uni.I18n.translate('general.active', 'WSS', 'Active');
                        } else {
                            return Uni.I18n.translate('general.inactive', 'WSS', 'Inactive');
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.type', 'WSS', 'Type'),
                    name: 'direction',
                    hidden: !me.isLandingPage,
                    renderer: function(value) {
                        if(Ext.isEmpty(value)) {
                            return '-';
                        }
                        return value.localizedValue;
                    }
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.webservice', 'WSS', 'Webservice'),
                    name: 'webServiceName'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.url', 'WSS', 'Url'),
                    name: 'url'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.logLevel', 'WSS', 'Log level'),
                    name: 'logLevel',
                    renderer: function (value) {
                        return value.localizedValue;
                    }

                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.traceRequests', 'WSS', 'Trace requests'),
                    name: 'tracing',
                    renderer: me.renderYesOrNo
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.traceRequestsFileName', 'WSS', 'Trace requests file name'),
                    name: 'traceFile'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.httpCompression', 'WSS', 'HTTP compression'),
                    name: 'httpCompression',
                    renderer: me.renderYesOrNo
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.schemeValidation', 'WSS', 'Scheme validation'),
                    name: 'schemaValidation',
                    renderer: me.renderYesOrNo
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.authenticationRequired', 'WSS', 'Authentication required'),
                    name: 'authenticated',
                    renderer: me.renderYesOrNo
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.userRole', 'WSS', 'User role'),
                    name: 'group',
                    renderer: function(value){
                        if(Ext.isEmpty(value)){
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
                    fieldLabel: Uni.I18n.translate('general.userName', 'WSS', 'Username'),
                    name: 'username',
                    renderer: function(value){
                        Ext.isEmpty(value)?this.hide():this.show();
                        return value;
                    }
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.password', 'WSS', 'Password'),
                    name: 'password',
                    renderer: function(value){
                        Ext.isEmpty(value)?this.hide():this.show();
                        return value;
                    }
                }
            ]
        };
        me.callParent(arguments);
    },

    renderYesOrNo: function (value) {
        if (value === true) {
            return Uni.I18n.translate('general.yes', 'WSS', 'Yes');
        } else {
            return Uni.I18n.translate('general.no', 'WSS', 'No')
        }
    }

});