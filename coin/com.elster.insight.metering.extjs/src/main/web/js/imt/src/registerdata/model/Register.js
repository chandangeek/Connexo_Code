Ext.define('Imt.registerdata.model.Register', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
             {name: 'readingType', type: 'auto'},
             {name: 'readingTypemRID', type: 'string', mapping: 'readingType.mRID', persist: false},
             {name: 'readingTypeFullAliasName', type: 'string', mapping: 'readingType.fullAliasName', persist: false},
             {name: 'lastReadingValue', type: 'auto'},
             {name: 'lastValueTimestamp', type: 'auto'}
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
