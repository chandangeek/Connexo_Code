Ext.define('Imt.registerdata.model.Reading', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'deviceName', type: 'string'},
        {name: 'readingTypemRID', type: 'string'},
        {name: 'readingTypeAlias', type: 'string'},
        {name: 'utcTimestamp', type: 'number'},
        {name: 'recordedTime', type: 'number'},
        {name: 'readingValue', type: 'number'},
    ],
});
