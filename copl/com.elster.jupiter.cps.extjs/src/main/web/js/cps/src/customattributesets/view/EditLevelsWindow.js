/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.view.EditLevelsWindow', {
    extend: 'Ext.window.Window',
    xtype: 'custom-attribute-set-edit-levels',
    itemId: 'custom-attribute-set-edit-levels-id',
    closable: false,
    width: 700,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    possibleValues: null,
    record: null,

    requires: [
        'Cps.customattributesets.view.widget.LevelsSelector'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                border: false,
                width: '100%',
                title: Uni.I18n.translate('customattributesets.editlevelsof', 'CPS', 'Edit levels of \'{0}\'', [Ext.String.htmlEncode(me.record.get('name'))]),
                ui: 'medium',
                items: [
                    {
                        xtype: 'container',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'custom-attribute-sets-levels-selector',
                                fieldLabel: Uni.I18n.translate('customattributesets.viewlevels', 'CPS', 'View levels'),
                                itemId: 'custom-attribute-sets-view-levels-selector',
                                margin: '0 250 0 0',
                                record: me.record,
                                defaultValuesField: 'defaultViewPrivileges',
                                filledValuesField: 'viewPrivileges'
                            },
                            {
                                xtype: 'custom-attribute-sets-levels-selector',
                                fieldLabel: Uni.I18n.translate('customattributesets.editlevels', 'CPS', 'Edit levels'),
                                itemId: 'custom-attribute-sets-edit-levels-selector',
                                record: me.record,
                                defaultValuesField: 'defaultEditPrivileges',
                                filledValuesField: 'editPrivileges'
                            }
                        ]}
                ],
                dockedItems: {
                    xtype: 'container',
                    dock: 'bottom',
                    margin: '20 0 0 150',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'save-attribute-set-levels',
                            ui: 'action',
                            text: Uni.I18n.translate('general.save', 'CPS', 'Save'),
                            handler: function () {
                                var editLevels = me.down('#custom-attribute-sets-edit-levels-selector').getValue(),
                                    viewLevels = me.down('#custom-attribute-sets-view-levels-selector').getValue();

                                me.record.set('editPrivileges', editLevels);
                                me.record.set('viewPrivileges', viewLevels);
                                me.fireEvent('saverecord', me.record);
                                me.close();
                            }
                        },
                        {
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancel-attribute-set-levels',
                            text: Uni.I18n.translate('general.cancel', 'CPS', 'Cancel'),
                            handler: function () {
                                me.close();
                            }
                        }
                    ]
                }
            }
        ];

        this.callParent(arguments);
    }
});