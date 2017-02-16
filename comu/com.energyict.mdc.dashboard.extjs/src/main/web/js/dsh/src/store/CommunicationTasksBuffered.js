/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.CommunicationTasksBuffered', {
    extend: 'Dsh.store.CommunicationTasks',
    buffered: true,
    pageSize: 50
});