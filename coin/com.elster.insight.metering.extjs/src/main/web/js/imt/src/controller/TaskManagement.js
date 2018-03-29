/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.controller.TaskManagement', {
    extend: 'Apr.controller.TaskManagement',
    // application key
    applicationKey: 'Insight',

    //main root
    rootRouteWithArguments: '#/administration/taskmanagement?application=Insight',
    rootRoute: 'administration/taskmanagement',
    rootRouteArguments: {application: 'Insight'},

    // add task route
    addTaskRoute: '#/administration/taskmanagement/add'
});
