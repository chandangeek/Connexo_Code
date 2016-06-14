Ext.define('Wss.view.PreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.webservices-preview-form',
    layout: {
        type: 'column'
    },

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
                    name: 'logLevel'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.traceRequests', 'WSS', 'Trace requests'),
                    name: 'tracing'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.traceRequestsFileName', 'WSS', 'Trace requests file name'),
                    name: 'tracing'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.httpCompression', 'WSS', 'HTTP compression'),
                    name: 'httpCompression'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.schemeValidation', 'WSS', 'Scheme validation'),
                    name: 'schemaValidation'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('webservices.authenticationRequired', 'WSS', 'Authentication required'),
                    name: 'authenticated'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.userRole', 'WSS', 'User role'),
                    name: 'authenticated'
                }
            ]
        };
        me.callParent(arguments);
    }

});