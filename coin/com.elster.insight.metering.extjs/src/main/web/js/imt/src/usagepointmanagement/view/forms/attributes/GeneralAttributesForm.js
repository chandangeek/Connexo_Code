Ext.define('Imt.usagepointmanagement.view.forms.attributes.GeneralAttributesForm', {
    extend: 'Imt.usagepointmanagement.view.forms.attributes.ViewEditForm',
    alias: 'widget.general-attributes-form',
    requires: [
        'Uni.form.field.Duration',
        'Imt.usagepointmanagement.view.forms.fields.DisplayFieldWithIcon'
    ],

    initComponent: function () {
        var me = this;

        me.viewForm = [
            {
                name: 'mRID',
                itemId: 'fld-up-mRID',
                fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'MRID'),
                renderer: function (value) {
                    return value ? Ext.htmlEncode(value) : '-';
                }
            },

            {
                xtype: 'displayfieldwithicon',
                name: 'serviceCategory',
                itemId: 'fld-up-serviceCategory',
                fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category')
            },
            {
                name: 'name',
                itemId: 'fld-up-name',
                fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                renderer: function (value) {
                    return value ? value : '-';
                }
            },
            {
                name: 'installationTime',
                itemId: 'fld-up-created',
                fieldLabel: Uni.I18n.translate('general.label.created', 'IMT', 'Created'),
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                }
            },
            {
                name: 'location',
                itemId: 'fld-up-location',
                fieldLabel: Uni.I18n.translate('general.label.location', 'IMT', 'Location'),
                renderer: function (value) {
                    return value ? value : '-';
                }
            },
            {
                name: 'typeOfUsagePoint',
                itemId: 'fld-up-typeOfUsagePoint',
                fieldLabel: Uni.I18n.translate('general.label.typeOfUsagePoint', 'IMT', 'Type of usage point'),
                renderer: function (value) {
                    var result;

                    if (!Ext.isEmpty(value)) {
                        result = Ext.getStore('Imt.usagepointmanagement.store.UsagePointTypes').findRecord('name', value).get('displayName');
                    }

                    return result || '-';
                }
            },
            {
                xtype: 'displayfieldwithicon',
                name: 'connectionState',
                itemId: 'fld-up-connectionState',
                fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state')
            },
            {
                name: 'readRoute',
                itemId: 'fld-up-readRoute',
                fieldLabel: Uni.I18n.translate('general.label.readRoute', 'IMT', 'Read route'),
                renderer: function (value) {
                    return value ? value : '-';
                }
            },
            {
                name: 'serviceDeliveryRemark',
                itemId: 'fld-up-serviceDeliveryRemark',
                fieldLabel: Uni.I18n.translate('general.label.serviceDeliveryRemark', 'IMT', 'Service delivery remark'),
                renderer: function (value) {
                    return value ? value : '-';
                }
            }
        ];

        me.editForm = [
            {
                xtype: 'displayfield',
                name: 'mRID',
                itemId: 'up-mrid-textfield',
                fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'MRID')
            },
            {
                xtype: 'displayfieldwithicon',
                name: 'serviceCategory',
                itemId: 'up-service-category-combo',
                fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category')
            },
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'up-name-textfield',
                fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name')
            },
            {
                xtype: 'displayfield',
                name: 'installationTime',
                itemId: 'fld-up-created',
                fieldLabel: Uni.I18n.translate('general.label.created', 'IMT', 'Created'),
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                }
            },
            {
                xtype: 'textfield',
                name: 'location',
                itemId: 'fld-up-location',
                fieldLabel: Uni.I18n.translate('general.label.location', 'IMT', 'Location')
            },
            {
                xtype: 'displayfield',
                name: 'typeOfUsagePoint',
                itemId: 'fld-up-typeOfUsagePoint',
                fieldLabel: Uni.I18n.translate('general.label.typeOfUsagePoint', 'IMT', 'Type of usage point'),
                renderer: function (data) {
                    var value;
                    value = Ext.getStore('Imt.usagepointmanagement.store.UsagePointTypes').findRecord('name', data);
                    return value.get('displayName');
                }
            },
            {
                xtype: 'displayfieldwithicon',
                name: 'connectionState',
                itemId: 'fld-up-connectionState',
                fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state')
            },
            {
                xtype: 'textfield',
                name: 'readRoute',
                itemId: 'fld-up-readRoute',
                fieldLabel: Uni.I18n.translate('general.label.readRoute', 'IMT', 'Read route')
            },
            {
                xtype: 'textfield',
                name: 'serviceDeliveryRemark',
                itemId: 'fld-up-serviceDeliveryRemark',
                fieldLabel: Uni.I18n.translate('general.label.serviceDeliveryRemark', 'IMT', 'Service delivery remark')
            }
        ];

        me.callParent();
    }
});