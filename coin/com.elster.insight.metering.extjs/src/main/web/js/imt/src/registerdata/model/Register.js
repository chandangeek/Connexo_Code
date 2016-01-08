Ext.define('Imt.registerdata.model.Register', {
    extend: 'Uni.model.ParentVersion',
    requires: [],
    fields: [
         {
             name: 'id', 
             type: 'string',
             mapping: function(data) {
                 return data.readingType.mRID;
             }
         },
         {
        	 name: 'name', 
        	 type: 'string',
        	 mapping: function(data) {
                 return data.readingType.fullAliasName;
             }
         },
         {name: 'readingType', type: 'auto'},
         {name: 'readingTypemRID', type: 'string', mapping: 'readingType.mRID', persist: false},
         {name: 'readingTypeFullAliasName', type: 'string', mapping: 'readingType.fullAliasName', persist: false},
         {name: 'lastReadingValue', type: 'auto'},
         {name: 'lastValueTimestamp', type: 'auto'},
         {name: 'unitOfMeasure', type: 'auto'},
         {name: 'timeOfUse', type: 'auto'},
         {name: 'isCumulative', type: 'boolean'},
         {name: 'validationInfo', type: 'auto'},
         {
             name: 'validationInfo_validationActive',
             persist: false,
             mapping: function (data) {
                 return (data.validationInfo && data.validationInfo.validationActive) ? Uni.I18n.translate('general.active', 'IMT', 'Active') : Uni.I18n.translate('general.inactive', 'IMT', 'Inactive');
             }
         },
         {
             name: 'validationInfo_dataValidated',
             persist: false,
             mapping: function (data) {
                 return (data.validationInfo && data.validationInfo.dataValidated) ? Uni.I18n.translate('general.yes', 'IMT', 'Yes')
                     : Uni.I18n.translate('general.no', 'IMT', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>';
             }
         },
         {
             name: 'validationInfo_lastChecked',
             persist: false,
             mapping: function (data) {
                 return (data.validationInfo && data.validationInfo.lastChecked)
                     ? Uni.DateTime.formatDateTimeLong(new Date(data.validationInfo.lastChecked))
                     : Uni.I18n.translate('general.never', 'IMT', 'Never');
             }
         },
     ],
     proxy: {
         type: 'rest',
         urlTpl: '/api/udr/usagepoints/{mRID}/registers/',
         timeout: 240000,
         reader: {
             type: 'json'
         },
         setUrl: function (params) {
             this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID));
         }
     }
});
