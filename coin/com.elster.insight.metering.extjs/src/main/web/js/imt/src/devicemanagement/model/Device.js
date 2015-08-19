Ext.define('Imt.devicemanagement.model.Device', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'mRID', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'aliasName', type: 'string'},
        {name: 'serialNumber', type: 'string'},
        {name: 'utcNumber', type: 'string'},
        {name: 'version', type: 'number'},
        {name: 'eMail1', type: 'string'},
        {name: 'eMail2', type: 'string'},
        {name: 'amrSystemName', type: 'string'},
        {name: 'usagePointMRId', type: 'string'},
        {name: 'usagePointName', type: 'string'},
        {name: 'installedDate', type: 'number'},
        {name: 'removedDate', type: 'number'},
        {name: 'retiredDate', type: 'number'},
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/devices',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterInfos'
        }
    }
});
