/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.AddDeviceStatesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'dbp-add-device-states-setup',
    overflowY: true,
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage',
        'Dbp.processes.view.AddDeviceStates'
    ],

    editProcessRecord: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('editProcess.addDeviceStates', 'DBP', 'Add device states'),
                itemId: 'pnl-select-device-states',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        selectByDefault: false,
                        grid: {
                            xtype: 'dbp-add-device-states-grid',
                            itemId: 'grd-add-device-states',
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
                            text: Uni.I18n.translate('editProcess.addDeviceStates.empty', 'DBP', 'All device states have been added to the process.')
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    onSelectionChange: function (selectionModel, selected) {
        this.down('[action=addSelectedDeviceStates]').setDisabled(!selected.length);
    }
});