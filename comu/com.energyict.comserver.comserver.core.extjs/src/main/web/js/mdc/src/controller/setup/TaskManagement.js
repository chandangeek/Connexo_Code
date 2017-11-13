/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.TaskManagement', {
    extend: 'Apr.controller.TaskManagement',
    // application key
    applicationKey: Uni.util.Application.getAppName(),

    //main root
    rootRoute: '#/administration/taskmanagement/',
    //rootRouteArguments: {application: Uni.util.Application.getAppName()},

    // add task route
    addTaskRoute: '#/administration/taskmanagement/add'
});
