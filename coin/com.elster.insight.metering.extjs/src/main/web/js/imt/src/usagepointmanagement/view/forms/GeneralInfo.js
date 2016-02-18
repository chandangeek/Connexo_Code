Ext.define('Imt.usagepointmanagement.view.forms.GeneralInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.general-info-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormEmptyMessage',
        'Uni.form.field.DateTime',
        'Imt.usagepointmanagement.view.forms.fields.MeasureField'
    ],
    isPossibleAdd: true,
    defaults: {
        labelWidth: 260,
        width: 595
    },

    initComponent: function () {
        var me = this;

        me.items = [
            me.isPossibleAdd ?
            {
                itemId: 'general-info-warning',
                xtype: 'uni-form-error-message',
                hidden: true
            } :
            {
                itemId: 'not-possible-add',
                xtype: 'uni-form-empty-message',
                text: Uni.I18n.translate('usagepoint.add.notPossibleAdd', 'IMT', 'You cannot add usage point due to service category available')
            },
            {
                xtype: 'textfield',
                name: 'mRID',
                itemId: 'up-mrid-textfield',
                fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'MRID'),
                required: true
            },
            me.isPossibleAdd ?
            {
                xtype: 'combobox',
                name: 'techInfoType',
                itemId: 'up-service-category-combo',
                fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category'),
                afterSubTpl: '<span class="field-additional-info" style="color: #686868; font-style: italic">'
                + Uni.I18n.translate('usagepoint.add.clarification.serviceCategory', 'IMT', 'Service categories that you can\'t edit due to insufficient privileges are not included')
                + '</span>',
                required: true,
                store: 'Imt.usagepointmanagement.store.ServiceCategories',
                displayField: 'displayName',
                valueField: 'name',
                queryMode: 'local',
                forceSelection: true,
                emptyText: Uni.I18n.translate('usagepoint.add.emptyText.serviceCategory', 'IMT', 'Select service category...'),
                listeners: {
                    errorchange: {
                        fn: function (field, error) {
                            if (field.rendered) {
                                field.getEl().down('.field-additional-info').setDisplayed(Ext.isEmpty(error));
                            }
                        }
                    }
                }
            } :
            {
                xtype: 'displayfield',
                itemId: 'up-service-category-displayfield',
                fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category'),
                required: true,
                htmlEncode: false,
                style: 'font-style: italic',
                value: '<span style="color: #686868; font-style: italic">'
                + Uni.I18n.translate('usagepoint.add.noServiceCategoryAvailable', 'IMT', 'No service category available due to insufficient privileges')
                + '</span>'
            },
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'up-name-textfield',
                fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name')
            },
            {
                xtype: 'date-time',
                name: 'createTime',
                itemId: 'up-createTime-textfield',
                fieldLabel: Uni.I18n.translate('general.label.created', 'IMT', 'Created'),
                required: true,
                layout: 'hbox',
                dateConfig: {
                    flex: 1
                },
                dateTimeSeparatorConfig: {
                    html: Uni.I18n.translate('general.at', 'IMT', 'At').toLowerCase(),
                    style: 'color: #686868'
                },
                hoursConfig: {
                    width: 64
                },
                minutesConfig: {
                    width: 64
                }
            },
            {
                xtype: 'textfield',
                name: 'location',
                itemId: 'up-location-combo',
                fieldLabel: Uni.I18n.translate('general.label.location', 'IMT', 'Location')
            },
            {
                xtype: 'combobox',
                name: 'typeOfUsagePoint',
                itemId: 'up-typeOfUsagePoint-combo',
                fieldLabel: Uni.I18n.translate('general.label.typeOfUsagePoint', 'IMT', 'Type of usage point'),
                store: 'Imt.usagepointmanagement.store.UsagePointTypes',
                displayField: 'displayValue',
                valueField: 'id',
                queryMode: 'local',
                forceSelection: true,
                emptyText: Uni.I18n.translate('usagepoint.add.emptyText.typeOfUsagePoint', 'IMT', 'Select type of usage point...'),
                listeners: {
                    change: {
                        fn: function (field, newValue) {
                            if (Ext.isEmpty(newValue)) {
                                field.reset();
                            }
                        }
                    }
                }
            },
            {
                xtype: 'textfield',
                name: 'readRoute',
                itemId: 'up-readRoute-textfield',
                fieldLabel: Uni.I18n.translate('general.label.readRoute', 'IMT', 'Read route')
            },
            {
                xtype: 'textfield',
                name: 'serviceDeliveryRemark',
                itemId: 'up-serviceDeliveryRemark-textfield',
                fieldLabel: Uni.I18n.translate('general.label.serviceDeliveryRemark', 'IMT', 'Service delivery remark')
            }
        ];

        me.callParent(arguments);
    }
});