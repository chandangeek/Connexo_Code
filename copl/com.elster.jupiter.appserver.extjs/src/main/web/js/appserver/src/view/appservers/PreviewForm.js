/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.appservers-preview-form',
    router: null,
    appServerName: null,
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
                fieldLabel: Uni.I18n.translate('general.name', 'APR', 'Name'),
                name: 'name'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.status', 'APR', 'Status'),
                name: 'status'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.exportPath', 'APR', 'Export path'),
                itemId: 'txt-export-path',
                name: 'exportDirectory'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.importPath', 'APR', 'Import path'),
                itemId: 'txt-import-path',
                name: 'importDirectory'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                itemId: 'messageServices',
                name: 'messageServicesCount',
                renderer: function (value) {
                    var result;
                    if (value === '') {
                        result = '-';
                    }
                    else {
                        result = Uni.I18n.translatePlural('general.messageServicesCount', value, 'APR', 'No message services', '{0} message service', '{0} message services');
                        var url = me.router.getRoute('administration/appservers/overview/messageservices').buildUrl({appServerName: me.appServerName});
                        result = '<a href="' + url + '">' + Ext.String.htmlEncode(result) + '</a>';
                    }
                    return result;
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.importServices', 'APR', 'Import services'),
                itemId: 'importServices',
                name: 'importServicesCount',
                renderer: function (value) {
                    var result;
                    if (value === '') {
                        result = '-';
                    } else {
                        result = Uni.I18n.translatePlural('general.importServicesCount', value, 'APR', 'No import services', '{0} import service', '{0} import services');
                        var url = me.router.getRoute('administration/appservers/overview/importservices').buildUrl({appServerName: me.appServerName});
                        result = '<a href="' + url + '">' + Ext.String.htmlEncode(result) + '</a>';
                    }
                    return result;
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.webserviceEndpoints', 'APR', 'Web service endpoints'),
                itemId: 'endPointConfigurations',
                name: 'webserviceEndpointsCount',
                renderer: function (value) {
                    var result;
                    if (value === '') {
                        result = '-';
                    } else {
                        result = Uni.I18n.translatePlural('general.webserviceEndpointsCount', value, 'APR', 'No web service endpoints', '{0} web service endpoint', '{0} web service endpoints');
                        var url = me.router.getRoute('administration/appservers/overview/webserviceendpoints').buildUrl({appServerName: me.appServerName});
                        result = '<a href="' + url + '">' + Ext.String.htmlEncode(result) + '</a>';
                    }
                    return result;
                }

            }
        ];
        me.callParent(arguments);
    },

        updateAppServerPreview: function (appServerRecord) {
            var me = this;
            me.appServerName = appServerRecord.get('name');

            if (!Ext.isDefined(appServerRecord)) {
                return;
            }
            if (me.rendered) {
                Ext.suspendLayouts();
            }

            me.loadRecord(appServerRecord);
            if (me.rendered) {
                Ext.resumeLayouts(true);
            }
        }

    });
