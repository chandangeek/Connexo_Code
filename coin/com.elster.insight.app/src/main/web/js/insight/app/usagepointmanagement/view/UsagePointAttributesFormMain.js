Ext.define('InsightApp.usagepointmanagement.view.UsagePointAttributesFormMain', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagePointAttributesFormMain',
    itemId: 'usagePointAttributesFormMain',
//    title: Uni.I18n.translate('usagePointManagement.metrologyConfiguration', 'MDC', 'Usage Point Attributes'),
//    router: null,
//    ui: 'tile',
    
    requires: [
        'Uni.form.field.Duration'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes', 'MDC', 'General attributes'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 150
                },
                items: [
                    {
                        name: 'mRID',
                        itemId: 'fld-up-mRID',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.mrid', 'MDC', 'mRID')
                    },
                    {
                        name: 'name',
                        itemId: 'fld-up-name',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.name', 'MDC', 'Name'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'serviceCategory',
                        itemId: 'fld-up-serviceCategory',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.serviceCategory', 'MDC', 'Service category'),
                    },
                    {
                        name: 'isSdp',
                        itemId: 'fld-up-sdp',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.sdp', 'MDC', 'SDP'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('usagePointManagement.yes', 'MDC', 'Yes') : Uni.I18n.translate('usagePointManagement.no', 'MDC', 'No');
                        }
                    },
                    {
                        name: 'isVirtual',
                        itemId: 'fld-up-virtual',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.virtual', 'MDC', 'Virtual'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('usagePointManagement.yes', 'MDC', 'Yes') : Uni.I18n.translate('usagePointManagement.no', 'MDC', 'No');
                        }
                    },
                    {
                        name: 'version',
                        itemId: 'fld-up-version',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.version', 'MDC', 'Version')
                    },
                    {
                        name: 'readCycle',
                        itemId: 'fld-up-version',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.readCycle', 'MDC', 'Read cycle'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'created',
                        itemId: 'fld-up-created',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.status', 'MDC', 'Created')
                    },
                    {
                        name: 'updated',
                        itemId: 'fld-up-updated',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.status', 'MDC', 'Last update'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    }
                ]
            }
        ];
        me.callParent();
    }
});