Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.PurposesPreview', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Imt.util.CommonFields'
    ],
    frame: true,
    alias: 'widget.purposes-preview',
    router: null,
    title: ' ',

    defaults: {
        xtype: 'fieldcontainer',
        labelAlign: 'top',
        hidden: true,
        defaults: {
            xtype: 'displayfield',
            labelWidth: 250
        }
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'fieldcontainer',
                itemId: 'purposes-preview-container',
                fieldLabel: Uni.I18n.translate('form.metrologyconfiguration.section.meterRoles', 'IMT', 'Meter roles')
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'purpose-preview-formula-components',
                fieldLabel: Uni.I18n.translate('general.attributes', 'IMT', 'Attributes')
            }
        ];
        me.callParent(arguments);
    },

    loadRecord: function (record, usagePoint) {
        var me = this,
            meterRolesContainer = me.down('#purposes-preview-container'),
            attributesContainer = me.down('#purpose-preview-formula-components'),
            purposeName = record.get('name'),
            meterRoles = me.prepareMeterRoles(record.get('meterRoles')),
            attributes = me.prepareAttributes(purposeName, usagePoint);

        Ext.suspendLayouts();
        me.setTitle(Ext.String.htmlEncode(purposeName));

        meterRolesContainer.removeAll();
        meterRolesContainer.add(meterRoles);
        meterRolesContainer.setVisible(!Ext.isEmpty(meterRoles));

        attributesContainer.removeAll();
        attributesContainer.add(attributes);
        attributesContainer.setVisible(!Ext.isEmpty(attributes));
        Ext.resumeLayouts(true);
    },

    prepareMeterRoles: function (meterRoles) {
        return _.map(meterRoles, function (meterRole) {
            var deviceLink;

            if (meterRole.meter) {
                if (meterRole.url) {
                    deviceLink = Ext.String.format('<a href="{0}" target="_blank">{1}</a>', meterRole.url, Ext.String.htmlEncode(meterRole.meter));
                } else {
                    deviceLink = Ext.String.htmlEncode(meterRole.meter);
                }
            } else {
                deviceLink = '-';
            }

            return {
                htmlEncode: false,
                fieldLabel: meterRole.name,
                itemId: meterRole.mRID,
                value: deviceLink
            };
        });
    },

    prepareAttributes: function (purposeName, usagePoint) {
        var me = this,
            usagePointPurposes = usagePoint.get('metrologyConfiguration').purposes,
            purpose = _.find(usagePointPurposes, function (purpose) {
                return purpose.name == purposeName
            }),
            properties = me.getAppropriateCustomProperties(purpose.readingTypeDeliverables, usagePoint.customPropertySets());

        return _.map(properties, function (property) {
            return {
                fieldLabel: property.get('name'),
                htmlEncode: false,
                value: me.formatCustomPropertyValue(property)
                + Imt.util.CommonFields.prepareCustomPropertyInfoIcon(property.customPropertySetName)
            }
        });
    },

    getAppropriateCustomProperties: function (readingTypeDeliverables, usagePointCustomPropertySets) {
        var properties = [];

        _.each(readingTypeDeliverables, function (readingTypeDeliverable) {
            _.each(readingTypeDeliverable.formula.customProperties, function (customProperty) {
                var appropriateCustomPropertySet = usagePointCustomPropertySets.findRecord('customPropertySetId', customProperty.customPropertySet.id),
                    appropriateCustomProperty;

                if (appropriateCustomPropertySet) {
                    appropriateCustomProperty = appropriateCustomPropertySet.properties().findRecord('key', customProperty.key);
                    if (appropriateCustomProperty) {
                        appropriateCustomProperty.customPropertySetName = appropriateCustomPropertySet.get('name');
                        properties.push(appropriateCustomProperty);
                    }
                }
            });
        });

        return properties;
    },

    formatCustomPropertyValue: function (property) {
        var valueRegExp = /(-?\d*)\:-?\d*\:.*/,
            propertyValue = property.getPropertyValue().get('value'),
            value;

        if (propertyValue && propertyValue.displayValue) {
            value = propertyValue.id.replace(valueRegExp, '$1') + ' ' + propertyValue.displayValue;
        } else if (!Ext.isEmpty(propertyValue)) {
            value = propertyValue;
        }

        return value || '-';
    }
});


