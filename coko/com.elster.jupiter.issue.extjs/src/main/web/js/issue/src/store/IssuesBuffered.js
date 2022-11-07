/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.IssuesBuffered', {
    extend: 'Isu.store.Issues',
    buffered: true,
    pageSize: 100,
    remoteFilter: true
});
