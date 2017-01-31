/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.PreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.webservices-preview-form',
    itemId: 'webservices-preview-form',
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
                    fieldLabel: Uni.I18n.translate('general.status', 'WSS', 'Status'),
                    name: 'active',
                    hidden: !me.isLandingPage,
                    renderer: function (value) {
                        if (value === true) {
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
                    renderer: function (value) {
                        if (Ext.isEmpty(value)) {
                            return '-';
                        }
                        return value.localizedValue;
                    }
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.webservice', 'WSS', 'Web service'),
                    name: 'webServiceName',
                    renderer: function (value) {
                        var record = this.up().getRecord();
                        if (record) {
                            if (value && record.get('available')) {
                                return value;
                            } else if (value && !record.get('available')) {
                                return value + ' (' + Uni.I18n.translate('general.notAvailable', 'WSS', 'not available') + ')' + '<span class="icon-warning" style="margin-left:5px; color:#eb5642;"></span>';
                            }
                        }
                        return '-';
                    }
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.url', 'WSS', 'Url'),
                    name: 'url',
                    itemId: 'pathField'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.url', 'WSS', 'Url'),
                    name: 'previewUrl',
                    hidden: true,
                    renderer: function (value) {
                        if (Ext.isEmpty(value)) {
                            this.hide();
                            this.up().down('#pathField').show();
                            return '-';
                        } else {
                            this.up().down('#pathField').hide();
                            this.show();
                            return value;
                        }
                    }
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
                    name: 'traceFile',
                    renderer: function (value, field) {
                        if (field.up('form').down('[name=tracing]').getValue() === '') {
                            this.hide();
                        } else {
                            this.show();
                        }
                        return value;
                    }
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
                    renderer: function (value, field) {
                        var rec = field.up('form').getRecord();
                        if (rec) {
                            rec.get('type') === 'REST' ? this.hide() : this.show();
                            if (value === true) {
                                return Uni.I18n.translate('general.yes', 'WSS', 'Yes');
                            } else {
                                return Uni.I18n.translate('general.no', 'WSS', 'No')
                            }
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.authenticationRequired', 'WSS', 'Authentication'),
                    name: 'authenticationMethod',
                    renderer: function (value) {
                        return !Ext.isEmpty(value) ? value.localizedValue : '';
                    }
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.userRole', 'WSS', 'User role'),
                    name: 'group',
                    renderer: function (value, field) {
                        if (field.up('form').down('[name=authenticationMethod]').getValue().id === "BASIC_AUTHENTICATION" && Ext.isEmpty(value) && field.up('form').getRecord().get('direction') === 'INBOUND') {
                            this.show();
                            return Uni.I18n.translate('endPointAdd.all', 'WSS', 'All');
                        }
                        else if (Ext.isEmpty(value)) {
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
                    renderer: function (value) {

                        Ext.isEmpty(value) ? this.hide() : this.show();
                        return value;
                    }
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.password', 'WSS', 'Password'),
                    name: 'password',
                    renderer: function (value) {
                        Ext.isEmpty(value) ? this.hide() : this.show();
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