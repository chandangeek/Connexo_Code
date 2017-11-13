/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.TaskManagement', {
    extend: 'Apr.controller.TaskManagement',
    applicationKey: Uni.util.Application.getAppName(),
    rootRoute: '#/administration/taskmanagement/',
    addTaskRoute: '#/administration/taskmanagement/add'
});
