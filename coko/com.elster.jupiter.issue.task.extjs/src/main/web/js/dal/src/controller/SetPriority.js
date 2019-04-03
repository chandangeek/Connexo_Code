/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.controller.SetPriority', {
    extend: 'Isu.controller.SetPriority',
    issueModel: 'Itk.model.Issue',

    init: function () {
        var me = this;

        me.control({
            'issue-set-priority button[action=savePriority]': {
                click: this.savePriority
            }
        });
    }

});
