/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.controller.TaskManagementCrlRequest', {
    extend: 'Ext.app.Controller',


    views: ['Mdc.crlrequest.view.AddEditCrlRequest'],
    stores: [
        'Mdc.model.MeterGroup',
        'Mdc.crlrequest.store.CrlRequests'
    ],
    refs: [
        //todo: form?
        {ref: 'crlRequestAddEditForm', selector: 'crl-request-addedit-tgm'}
    ],

    init: function () {
        Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
            name: Uni.I18n.translate('general.crlRequest', 'MDC', 'CRL Request'),
            controller: this
        });
    },

    canView: function () {
        return true;
        // return Mdc.privileges.CrlRequest.canView();
    },

    canAdministrate: function () {
        return true;
        // return Mdc.privileges.CrlRequest.canEdit();
    },

    canEdit: function () {
        return true;
        // return Mdc.privileges.CrlRequest.canEdit();
    },

    canSetTriggers: function () {
        return true;
        // return Mdc.privileges.CrlRequest.canEdit();
    },

    canRemove: function () {
        return true;
        // return Mdc.privileges.CrlRequest.canEdit();
    },

    canRun: function () {
        return false;
    },

    canHistory: function () {
        return false;
    },

    getType: function () {
        return 'CrlRequest';
    },

    getTaskForm: function (caller, completedFunc) {
        var form = Ext.create('Mdc.crlrequest.view.AddEditCrlRequest');
        completedFunc.call(caller, form);
        return form;
    },


});
