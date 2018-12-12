/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.communication.OverviewDashboard', {
    extend: 'Dsh.model.communication.Overview',
    proxy: {
        type: 'ajax',
        url: '/api/dsr/communicationoverview/widget'
    }
});