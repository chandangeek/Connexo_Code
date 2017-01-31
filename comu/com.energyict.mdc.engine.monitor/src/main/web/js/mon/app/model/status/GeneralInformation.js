/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.model.status.GeneralInformation', {
    extend: 'Ext.data.Model',
    storeId: 'generalInfo',
    fields: ['changeDetectionFrequency', 'changeDetectionNextRun', 'pollingFrequency', 'eventRegistrationUri']
});