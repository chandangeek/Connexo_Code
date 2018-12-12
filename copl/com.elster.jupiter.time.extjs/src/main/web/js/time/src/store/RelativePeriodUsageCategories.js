/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.store.RelativePeriodUsageCategories', {
    extend: 'Tme.store.RelativePeriodCategories',
    storeId: 'relativePeriodUsageCategories',
    listeners: {
        load: function() {
            this.filter(function(rec){
                var key = rec.get('key');
                return key !== 'relativeperiod.category.usagepoint.validationOverview';
            });
        }
    }
});
