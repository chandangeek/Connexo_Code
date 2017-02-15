/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.workgroup.AddUsersToWorkgroup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usr-add-users-to-workgroup',
    overflowY: true,

    requires: [
        'Uni.view.grid.SelectionGrid'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workgroups.addUsersToWorkgroup', 'USR', 'Select users'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'selection-grid',
                    itemId: 'grd-user-selection',
                    width: '100%',
                    maxHeight: 395,
                    columns: [
                        {
                            header: Uni.I18n.translate('workgroups.addUsers.userName', 'USR', 'User name'),
                            dataIndex: 'name',
                            flex: 1
                        },
                        {
                            header: Uni.I18n.translate('workgroups.addUsers.status', 'USR', 'Status'),
                            dataIndex: 'statusDisplay',
                            flex: 1
                        }
                    ],
                    counterTextFn: function (count) {
                        return Uni.I18n.translatePlural('workgroups.addUsers.nrOfUsers.selected', count, 'USR',
                            'No users selected', '{0} users selected', '{0} users selected'
                        );
                    }
                },
                {
                    xtype: 'container',
                    itemId: 'buttonsContainer',
                    defaults: {
                        xtype: 'button'
                    },
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'USR', 'Add'),
                            name: 'add',
                            itemId: 'btn-add-users',
                            ui: 'action',
                            disabled: true
                        },
                        {
                            name: 'cancel',
                            itemId: 'lnk-cancel-add-users',
                            text: Uni.I18n.translate('general.cancel', 'USR', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);

        me.down('#grd-user-selection').on('selectionchange', function (model, records, eOpts) {
            var toggleFn = records.length === 0 ? 'disable' : 'enable';
            me.down('#btn-add-users')[toggleFn]();
        });
        me.down('#btn-add-users').on('click', me.onAddButtonClick, me);
    },

    onAddButtonClick: function () {
        var me = this;
        me.fireEvent('selecteditemsadd', me.down('#grd-user-selection')
            .getSelectionModel()
            .getSelection()
            .sort(function compare(a, b) {
                    if (a.get('name') < b.get('name'))
                        return -1;
                    if (a.get('name') > b.get('name'))
                        return 1;
                    return 0;
                }
            ));
    }
});
