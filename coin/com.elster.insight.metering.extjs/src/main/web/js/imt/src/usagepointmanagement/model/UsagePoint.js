Ext.define('Imt.usagepointmanagement.model.UsagePoint', {
    extend: 'Ext.data.Model',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'
    ],
    idProperty: 'mRID',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'mRID', type: 'string'},
        {name: 'serviceCategory', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'installationTime', type: 'int', defaultValue: null, useNull: true},
        {
            name: 'geoCoordinates',
            type: 'auto'
        },
        {
            name: 'location',
            type: 'auto',
            convert: function (value, record) {
                return value != ""? value : record.get('geoCoordinates');
            }
        },
        {name: 'version', type: 'int'},
        {name: 'purposes', type: 'auto', useNull: true, persist: false},
        {
            name: 'typeOfUsagePoint',
            persist: false,
            mapping: function(data){
                if (data.isSdp && data.isVirtual) {
                    return 'UNMEASURED_SDP';
                }
                if (data.isSdp && !data.isVirtual) {
                    return 'MEASURED_SDP';
                }
                if (!data.isSdp && !data.isVirtual) {
                    return 'MEASURED_NON_SDP';
                }
                if (!data.isSdp && data.isVirtual) {
                    return 'UNMEASURED_NON_SDP';
                }
            },
            // workaround for broken functionality of 'Ext.data.Field.serialize' in 'Uni.override.JsonWriterOverride.getRecordData'
            convert: function (value, record) {
                record.beginEdit();
                if (value) {
                    switch (value) {
                        case 'UNMEASURED_SDP':
                            record.set('isSdp', true);
                            record.set('isVirtual', true);
                            break;
                        case 'MEASURED_SDP':
                            record.set('isSdp', true);
                            record.set('isVirtual', false);
                            break;
                        case 'MEASURED_NON_SDP':
                            record.set('isSdp', false);
                            record.set('isVirtual', false);
                            break;
                        case 'UNMEASURED_NON_SDP':
                            record.set('isSdp', false);
                            record.set('isVirtual', true);
                            break;
                    }
                } else {
                    record.set('isSdp', null);
                    record.set('isVirtual', null);
                }
                record.endEdit();
                return value;
            }
        },
        {name: 'isSdp', type: 'boolean', useNull: true},
        {name: 'isVirtual', type: 'boolean', useNull: true},
        {name: 'readRoute', type: 'string'},
        {name: 'connectionState', type: 'auto', defaultValue: null},
        {name: 'servicePriority', type: 'string'},
        {name: 'serviceDeliveryRemark', type: 'string'},
        {name: 'techInfo', type: 'auto', defaultValue: {}},
        {name: 'metrologyConfiguration', type: 'auto', defaultValue: null},
        {name: 'meterActivations', type: 'auto'},
        {
            name: 'metrologyConfiguration_id',
            persist: false,
            mapping: 'metrologyConfiguration.id'
        },
        {
            name: 'metrologyConfiguration_activationTime',
            persist: false,
            type: 'date',
            dateFormat: 'time',
            mapping: 'metrologyConfiguration.activationTime'
        },
        {
            name: 'metrologyConfiguration_name',
            persist: false,
            mapping: 'metrologyConfiguration.name'
        },
        {
            name: 'metrologyConfiguration_status',
            persist: false,
            mapping: 'metrologyConfiguration.status'
        },
        {
            name: 'metrologyConfiguration_meterRoles',
            persist: false,
            mapping: 'metrologyConfiguration.meterRoles'
        },
        {
            name: 'metrologyConfiguration_purposes',
            persist: false,
            mapping: 'metrologyConfiguration.purposes'
        }
    ],

    associations: [
        {
            name: 'customPropertySets',
            associationKey: 'customPropertySets',
            type: 'hasMany',
            model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'
        }
    ],

    activateMeters: function (callback, failure) {
        var me = this;
        Ext.Ajax.request({
            url: '../../api/udr/usagepoints/' + encodeURIComponent(me.get('mRID')) + '/activatemeters',
            method: 'PUT',
            jsonData: Ext.JSON.encode(me.getProxy().getWriter().getRecordData(me)),
            success: callback,
            failure: failure
        })
    },

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/',
        timeout: 240000,
        reader: {
            type: 'json'
        }
    }
});