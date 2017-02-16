/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.ConnectionTasksBuffered', {
    extend: 'Dsh.store.ConnectionTasks',
    buffered: true,
    pageSize: 200
});