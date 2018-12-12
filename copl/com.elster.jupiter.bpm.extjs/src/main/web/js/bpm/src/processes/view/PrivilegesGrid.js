/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.view.PrivilegesGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    store: 'Bpm.processes.store.Privileges',
    alias: 'widget.privileges-grid',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('editProcess.nrOfPrivileges.selected', count, 'BPM',
            'No privileges selected', '{0} privilege selected', '{0} privileges selected'
        );
    },

    width: '100%',
    maxHeight: 300,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('editProcess.privilege', 'BPM', 'Privilege'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('editProcess.userRoles', 'BPM', 'User roles'),
                dataIndex: 'userRoles',
                renderer: function (value) {
                    var resultArray = [];
                    Ext.Array.each(value, function (userRole) {
                        resultArray.push(Ext.String.htmlEncode(userRole.name));
                    });
                    return resultArray.join('<br>');
                },
                flex: 1
            }
        ];
        me.callParent();
    }
});
