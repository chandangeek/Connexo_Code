Ext.define('Imt.usagepointmanagement.view.landingpageattributes.UsagePointMainAttributesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usage-point-main-attributes-panel',
    itemId: 'GENERAL-attribute-set',

    requires: [
        'Imt.usagepointmanagement.view.landingpageattributes.GeneralAttributesForm',
        'Imt.usagepointmanagement.view.SetupActionMenu'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    record: null,


    initComponent: function () {
        var me = this,
            action = Ext.create('Ext.menu.Item', {
                itemId: 'action-menu-general-attributes-form',
                menuItemClass: 'inlineEditableAttributeSet',
                editAvailable:true,
//                    privileges: Imt.privileges.Device.administrateDeviceData,
                text: Uni.I18n.translate('general.editGeneralInformation', 'IMT', "Edit 'General information'"),
                handler: function () {
                    if(this.editAvailable){
                        me.toEditMode(true, this);
                    } else {
                        console.log('cant edit');
                        //var confirmationWindow = Ext.create('edit-usage-point-confirmation-window');
                        //console.log(confirmationWindow);
                        //confirmationWindow.show();
                        var viewport = Ext.ComponentQuery.query('usage-point-management-setup')[0];

                        var confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                                confirmText: Uni.I18n.translate('general.editGeneralInformation.discard', 'IMT', 'Discard'),
                                confirmation: function () {
                                    //var cancelBtnArray = Ext.ComponentQuery.query('#edit-attributes-cancel-btn');

                                    viewport.doLayout();

                                    //Ext.each(cancelBtnArray, function (item) {
                                    //    //item.setDisabled(disabled);
                                    //
                                    //    item.fireEvent('click');
                                    //    //console.log(item.fireEvent('click'))
                                    //});


                                    me.toEditMode(true, this);
                                }
                            });

                        confirmationWindow.insert(1,
                            {
                                xtype: 'panel',
                                itemId: 'date-errors',
                                hidden: true,
                                bodyStyle: {
                                    color: '#eb5642',
                                    padding: '0 0 15px 65px'
                                },
                                html: ''
                            }
                        );

                        confirmationWindow.show({
                            msg: Uni.I18n.translate('general.editGeneralInformation.lostData', 'IMT', 'You will lost unsolved data.'),
                            title: Uni.I18n.translate('general.editGeneralInformation.discardChanges', 'IMT', "Discard 'General information' changes?")
                        });
                    }
                }
            });

        me.items = [
            {
                xtype: 'title-with-edit-button',
                pencilBtnItemId: '',
                editAvailable:true,
                title: Uni.I18n.translate('general.generalInformation', 'IMT', 'General information'),
                editHandler: function () {
                    if(this.editAvailable){
                        me.toEditMode(true, action);
                    } else {
                        console.log('cant edit');
                    }
                }
            },
            {
                xtype: 'container',
                hidden: true,
                itemId: 'bottom-buttons',
                dock: 'bottom',
                margin: '20 0 0 265',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'button',
                        ui: 'action',
                        text: Uni.I18n.translate('general.save', 'IMT', 'Save'),
                        action: 'save',
                        listeners: {
                            click: me.onSaveClick,
                            scope: me
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'edit-attributes-cancel-btn',
                        ui: 'link',
                        text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                        handler: function () {
                            me.toEditMode(false, action);
                        }
                    }
                ]
            }
        ];
        me.callParent();

        me.addAttributes('general-attributes-form', action);
    },

    addAttributes: function (actualForm, action) {
        var me = this,
            actionMenuArray = Ext.ComponentQuery.query('usage-point-setup-action-menu');


        Ext.suspendLayouts();

        Ext.each(actionMenuArray, function (menu) {
            menu.add(action);
        });

        me.add(1, {
            xtype: actualForm
        });

        Ext.resumeLayouts(true);

        me.down('#edit-form').getForm().loadRecord(me.record);
        me.down('#view-form').getForm().loadRecord(me.record);
    },

    toEditMode: function (isEdit, action) {
        var me = this;

        Ext.suspendLayouts();
        if (isEdit) {
            me.down('#pencil-btn').hide();
            me.down('#view-form').hide();
            me.down('#edit-form').show();
            me.down('#bottom-buttons').show();
            action.hide();
        } else {
            me.down('#pencil-btn').show();
            me.down('#view-form').show();
            me.down('#edit-form').hide();
            me.down('#bottom-buttons').hide();
            action.show();
        }
        Ext.resumeLayouts(true);

        me.down('#edit-form').loadRecord(me.record);
        Imt.customattributesonvaluesobjects.service.ActionMenuManager.setAvailableEditBtns(!isEdit);
    },

    onSaveClick: function () {
        var me = this,
            form = me.down('#edit-form');
        var values = form.getValues(),
            record = me.record.copy(me.record.get('mRID'));
        record.set(values);
        me.fireEvent('saveClick', form, record);
    }
});