/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.AddPrivilegesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'dbp-add-privileges-setup',
    overflowY: true,
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage',
        'Dbp.processes.view.AddPrivileges'
    ],

    editProcessRecord: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('editProcess.addPrivileges', 'DBP', 'Add privileges'),
                itemId: 'pnl-select-privileges',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        selectByDefault: false,
                        grid: {
                            xtype: 'dbp-add-privileges-grid',
                            itemId: 'grd-add-privileges',
                            hrefCancel: '',
                            listeners: {
                                selectionchange: {
                                    fn: Ext.bind(me.onSelectionChange, me)
                                }
                            }
                        },
                        emptyComponent: {
                            xtype: 'uni-form-empty-message',
                            itemId: 'ctr-add-device-states',
                            text: Uni.I18n.translate('editProcess.addPrivileges.empty', 'DBP', 'All privileges have been added to the process.')
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    onSelectionChange: function (selectionModel, selected) {
        this.down('[action=addSelectedPrivileges]').setDisabled(!selected.length);
    }
});