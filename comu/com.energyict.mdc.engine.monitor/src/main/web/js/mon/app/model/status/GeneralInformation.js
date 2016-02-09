Ext.define('CSMonitor.model.status.GeneralInformation', {
    extend: 'Ext.data.Model',
    storeId: 'generalInfo',
    fields: ['changeDetectionFrequency', 'changeDetectionNextRun', 'pollingFrequency', 'eventRegistrationUri']
});