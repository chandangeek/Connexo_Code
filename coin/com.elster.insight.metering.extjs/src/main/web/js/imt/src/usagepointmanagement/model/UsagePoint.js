Ext.define('Imt.usagepointmanagement.model.UsagePoint', {
    extend: 'Ext.data.Model',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'
    ],
    idProperty: 'mRID',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'mRID', type: 'string'},
        {name: 'serviceCategory', type: 'auto', defaultValue: null},
        {name: 'name', type: 'string'},
        {name: 'version', type: 'int'},
        //{name: 'createTime', type: 'date', dateFormat: 'time', defaultValue: new Date().getTime()},
        {name: 'createTime', type: 'int'},
        {name: 'location', type: 'string'},
        {
            name: 'typeOfUsagePoint',
            persist: false,
            mapping: function(data){
                if (data.isSdp && data.isVirtual) {
                    return 'UNMEASURED';
                }
                if (data.isSdp && !data.isVirtual) {
                    return 'SMART_DUMB';
                }
                if (!data.isSdp && !data.isVirtual) {
                    return 'INFRASTRUCTURE';
                }
                if (!data.isSdp && data.isVirtual) {
                    return 'N_A';
                }
            },
            // workaround for broken functionality of 'Ext.data.Field.serialize' in 'Uni.override.JsonWriterOverride.getRecordData'
            convert: function (value, record) {
                record.beginEdit();
                if (value) {
                    switch (value) {
                        case 'UNMEASURED':
                            record.set('isSdp', true);
                            record.set('isVirtual', true);
                            break;
                        case 'SMART_DUMB':
                            record.set('isSdp', true);
                            record.set('isVirtual', false);
                            break;
                        case 'INFRASTRUCTURE':
                            record.set('isSdp', false);
                            record.set('isVirtual', false);
                            break;
                        case 'N_A':
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
            },

        },
        {name: 'isSdp', type: 'boolean', useNull: true},
        {name: 'isVirtual', type: 'boolean', useNull: true},
        {name: 'readRoute', type: 'string'},
        {name: 'connectionState', type: 'string', defaultValue: 'UNKNOWN'},
        {name: 'servicePriority', type: 'string'},
        {name: 'serviceDeliveryRemark', type: 'string'},
        {name: 'techInfo', type: 'auto', defaultValue: null},
        {name: 'techInfoType', type: 'string'},
        {
            name: 'created',
            persist: false,
            mapping: function(data){
                return Uni.DateTime.formatDateTimeLong(new Date(data.createTime));
            }
        },
    ],

    associations: [
        {
            name: 'customProperties',
            associationKey: 'customProperties',
            type: 'hasMany',
            model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/',
        timeout: 240000,
        reader: {
            type: 'json'
        }
    }
});