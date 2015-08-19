Ext.define('Imt.usagepointmanagement.model.UsagePoint', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'mRID', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'aliasName', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'outageRegion', type: 'string'},
        {name: 'readCycle', type: 'string'},
        {name: 'readRoute', type: 'string'},
        {name: 'servicePriority', type: 'string'},
        {name: 'serviceCategory', type: 'string'},
        {name: 'serviceDeliveryRemark', type: 'string'},
        {name: 'amiBillingReady', type: 'auto'},
        {name: 'connectionState', type: 'string', defaultValue: 'UNKNOWN'},
        {name: 'serviceLocationID', type: 'number', defaultValue: 0},
        {name: 'checkBilling', type: 'boolean'},
        {name: 'isSdp', type: 'boolean'},
        {name: 'isVirtual', type: 'boolean'},
        {name: 'minimalUsageExpected', type: 'boolean'},
        {name: 'ratedCurrent', type: 'auto'},
        {name: 'version', type: 'number', useNull: true},
        //For ELECTRICITY
        {name: 'nominalServiceVoltage', type: 'auto'},
        {name: 'ratedCurrent', type: 'auto'},
        {name: 'ratedPower', type: 'auto'},
        {name: 'estimatedLoad', type: 'auto'},
        {name: 'grounded', type: 'boolean'},
        {name: 'phaseCode', type: 'string', defaultValue: 'UNKNOWN'},
        //For WATER
        {name: 'water', type: 'string'},
        //For GAS
        {name: 'gas', type: 'string'},
        {
            name: 'created',
            persist: false,
            mapping: function(data){
                return Uni.DateTime.formatDateTimeLong(new Date(data.createTime));
            }
        },
        {
            name: 'updated',
            persist: false,
            mapping: function(data){
                return Uni.DateTime.formatDateTimeLong(new Date(data.modTime));
            }
        },
        {
            name: 'start',
            persist: false,
            mapping: function(data){

                if(data && data.startTime)
                    return Uni.DateTime.formatDateTimeLong(new Date(data.startTime));
                else return '-';
            }
        },
        {
            name: 'end',
            persist: false,
            mapping: function(data){
                if(data && data.endTime)
                    return Uni.DateTime.formatDateTimeLong(new Date(data.endTime));
                else return '-';
            }
        },
        {
            name: 'mainAddress',
            persist: false,
            mapping: 'serviceLocation.direction'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'usagePoints'
        }
    }
});
