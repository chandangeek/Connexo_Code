/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.TaskManagement', {
    extend: 'Apr.controller.TaskManagement',
    // application key
    applicationKey: 'MultiSense',

    //main root
    rootRouteWithArguments: '#/administration/taskmanagement?application=MultiSense',
    rootRoute: 'administration/taskmanagement',
    rootRouteArguments: {application: 'MultiSense'},

    // add task route
    addTaskRoute: '#/administration/taskmanagement/add'
});
