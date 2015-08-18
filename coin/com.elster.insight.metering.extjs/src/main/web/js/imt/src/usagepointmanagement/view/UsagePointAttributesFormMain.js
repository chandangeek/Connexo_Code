Ext.define('Imt.usagepointmanagement.view.UsagePointAttributesFormMain', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagePointAttributesFormMain',
    itemId: 'usagePointAttributesFormMain',
//    title: Uni.I18n.translate('usagePointManagement.attributes', 'IMT', 'Usage Point Attributes'),
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
                fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes', 'IMT', 'General attributes'),
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
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.mrid', 'IMT', 'mRID')
                    },
                    {
                        name: 'name',
                        itemId: 'fld-up-name',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.name', 'IMT', 'Name'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'aliasName',
                        itemId: 'fld-up-aliasName',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.aliasName', 'IMT', 'Alias name'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'description',
                        itemId: 'fld-up-description',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.description', 'IMT', 'Description'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'serviceCategory',
                        itemId: 'fld-up-serviceCategory',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.serviceCategory', 'IMT', 'Service category'),
                    },
                    {
                        name: 'isSdp',
                        itemId: 'fld-up-sdp',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.sdp', 'IMT', 'SDP'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('usagePointManagement.yes', 'IMT', 'Yes') : Uni.I18n.translate('usagePointManagement.no', 'IMT', 'No');
                        }
                    },
                    {
                        name: 'isVirtual',
                        itemId: 'fld-up-virtual',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.virtual', 'IMT', 'Virtual'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('usagePointManagement.yes', 'IMT', 'Yes') : Uni.I18n.translate('usagePointManagement.no', 'IMT', 'No');
                        }
                    },
                    {
                        name: 'version',
                        itemId: 'fld-up-version',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.version', 'IMT', 'Version')
                    },
                    {
                        name: 'readCycle',
                        itemId: 'fld-up-version',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.readCycle', 'IMT', 'Read cycle'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'checkBilling',
                        itemId: 'fld-up-checkBilling',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.checkBilling', 'IMT', 'Check billing'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'amiBillingReady',
                        itemId: 'fld-up-amiBillingReady',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.amiBillingReady', 'IMT', 'AMI billing ready'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'outageRegion',
                        itemId: 'fld-up-outageRegion',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.outageRegion', 'IMT', 'Outage region'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'readRoute',
                        itemId: 'fld-up-readRoute',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.readRoute', 'IMT', 'Read route'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'servicePriority',
                        itemId: 'fld-up-servicePriority',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.servicePriority', 'IMT', 'Service priority'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'serviceDeliveryRemark',
                        itemId: 'fld-up-serviceDeliveryRemark',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.serviceDeliveryRemark', 'IMT', 'Service delivery remark'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'connectionState',
                        itemId: 'fld-up-connectionState',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.connectionState', 'IMT', 'Connection state'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'created',
                        itemId: 'fld-up-created',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.created', 'IMT', 'Created')
                    },
                    {
                        name: 'updated',
                        itemId: 'fld-up-updated',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.lastUpdate', 'IMT', 'Last update'),
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