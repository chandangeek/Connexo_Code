Ext.define('Dbp.processes.view.EditProcess', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-edit-process',
    requires: [
        'Uni.util.FormErrorMessage'
    ],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                itemId: 'frm-edit-process',
                ui: 'large',              
                defaults: {
                    labelWidth: 250,
                    width: 600,
                    enforceMaxLength: true
                },
                items: [
                    {
						xtype: 'combobox',
						itemId: 'cbo-associated-to',
						name: 'associatedTo',
						width: 600,
						fieldLabel: Uni.I18n.translate('editProcess.associatedTo', 'DBP', 'Start on'),
						labelWidth: 250,
						required: true,
						store: 'Dbp.processes.store.Associations',
						editable: false,											
						allowBlank: false,
						queryMode: 'local',
						displayField: 'name',
						valueField: 'value'
					},
					{
                        xtype: 'form',
                        title: Uni.I18n.translate('editProcess.startWhenInDeviceState', 'DBP', 'Start when in device state'),
                        ui: 'medium',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
						itemId: 'device-states-form',
                        items: [
						]
					},
					{
                        xtype: 'form',
                        title: Uni.I18n.translate('editProcess.startWhenAllowed', 'DBP', 'Start when allowed'),
                        ui: 'medium',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
						itemId: 'processes-form',
                        items: [
						]
					},                   
                    {
                        xtype: 'container',
                        margin: '0 0 0 265',
                        layout: 'hbox',
                        items: [
                            {
                                text: Uni.I18n.translate('general.save', 'DBP', 'Save'),
                                xtype: 'button',
                                ui: 'action',
                                itemId: 'btn-save'
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.cancel', 'DBP', 'Cancel'),
                                href: '#/administration/managementprocesses',
                                itemId: 'btn-cancel-link',
                                ui: 'link'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit, me.returnLink);
    }
});

