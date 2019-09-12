/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.BufferedIssues', {

    extend: 'Isu.store.Issues',

    buffered: true,

    pageSize: 200,

    remoteFilter: true

});