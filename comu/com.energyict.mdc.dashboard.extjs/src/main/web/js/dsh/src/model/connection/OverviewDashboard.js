/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.connection.OverviewDashboard', {
    extend: 'Dsh.model.connection.Overview',
    proxy: {
        type: 'ajax',
        url: '/api/dsr/connectionoverview/widget'
    }
});