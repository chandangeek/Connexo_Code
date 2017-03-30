/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.store.RelativePeriodsWithCount', {
    extend: 'Uni.property.store.RelativePeriods',
    proxy: {
        type: 'rest',
        url: '../../api/dal/creationrules/relativeperiods',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'relativePeriods'
        }
    }
});
