/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.component.AssigneeControl', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.issues-assignee-control',
    itemId: 'issues-assignee-control',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    defaults: {
        xtype: 'container',
        layout: {
            type: 'hbox'
        },
        margin: '0 0 8 0'
    },

    getValue: function () {
        //this was done in scope of JP-8241. We will have only users assignee for now
        return 'issueAssigneeUser';
    },

    markInvalid: function (errors) {
        this.down('combobox').markInvalid(errors);
    }
});