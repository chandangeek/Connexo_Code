/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.SetQueue', {
    extend: 'Ext.form.RadioGroup',
    alias: 'widget.set-queue',
    columns: 1,
    defaults: {
        name: 'setQueue'
    },
    requires: [
        'Apr.store.TasksType',
        'Uni.view.form.ComboBoxWithEmptyComponent'
    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'comboboxwithemptycomponent',
                fieldLabel: Uni.I18n.translate('general.selectqueue', 'APR', 'Select queue'),
                itemId: 'cmb-queue',

                config: {
                    name: 'queue',
                    emptyText: Uni.I18n.translate('general.selectqueue', 'APR', 'Select queue'),
                    store: 'Apr.store.TasksType',
                    queryMode: 'local',
                    displayField: 'queue',
                    noObjectsText: Uni.I18n.translate('general.noQueue', 'APR', 'No queue defined for selected task type'),
                    valueField: 'queue',
                    required: true,
                    allowBlank: false,
                    editable: false,
                    width: 400,
                    listeners: {
                        afterrender: function (field) {
                            field.focus(false, 200);
                        }
                    }
                },
                prepareLoading: function (store) {
                    store.getProxy().setUrl(me.record.getId());
                    return store;
                },
                style: {
                    margin: '0px 10px 0 10px'
                }
            }
        ];

        me.callParent(arguments);
    }
});