Ext.define('InsightApp.model.UsagePoint', {
    extend: 'Ext.data.Model',
    requires: [
       'InsightApp.model.Quantity'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'mRID', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'serviceCategory', type: 'string'},
        {name: 'serviceDeliveryRemark', type: 'string'},
        {name: 'amiBillingReady', type: 'string'},
        {name: 'connectionState', type: 'string'},
        {name: 'serviceLocationID', type: 'number', useNull: true},
        {name: 'checkBilling', type: 'boolean'},
        {name: 'isSdp', type: 'boolean'},
        {name: 'isVirtual', type: 'boolean'},
        {name: 'minimalUsageExpected', type: 'boolean'},
        'nominalServiceVoltage',
        {name: 'nominalVoltage', type: 'auto'},
        {name: 'version', type: 'number', useNull: true},
        {name: 'deviceMRID', type: 'string',persist: false, mapping:'meterActivationInfos.meterActivations[0].meter.mRID'},
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
    hasOne: {
    	    name: 'nominalServiceVoltage',
        	model: 'InsightApp.model.Quantity',
        	associationName: 'nominalServiceVoltage',
        	associationKey: 'nominalServiceVoltage',
        	setterName:'setNominalServiceVoltage',
        	getterName: 'getNominalServiceVoltage'
    },
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
