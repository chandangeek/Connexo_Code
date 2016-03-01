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
        var me = this;
            me.action = Ext.create('Ext.menu.Item', {
                itemId: 'action-menu-general-attributes-form',
                menuItemClass: 'inlineEditableAttributeSet',
                editAvailable: true,
//                    privileges: Imt.privileges.Device.administrateDeviceData,
                text: Uni.I18n.translate('general.editGeneralInformation', 'IMT', "Edit 'General information'"),
                handler: function () {
                    if (this.editAvailable) {
                        me.toEditMode(true);
                    } else {
                        me.showConfirmationWindow();
                    }
                }
            });

        me.items = [
            {
                xtype: 'title-with-edit-button',
                pencilBtnItemId: '',
                editAvailable: true,
                hiddenBtn: !Imt.privileges.UsagePoint.canAdministrate(),
                title: Uni.I18n.translate('general.generalInformation', 'IMT', 'General information'),
                editHandler: function () {
                    if (this.editAvailable) {
                        me.toEditMode(true);
                    } else {
                        me.showConfirmationWindow();
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
                            me.toEditMode(false);
                        }
                    }
                ]
            }
        ];
        me.callParent();

        me.addAttributes('general-attributes-form');
    },

    addAttributes: function (actualForm) {
        var me = this,
            actionMenuArray = Ext.ComponentQuery.query('usage-point-setup-action-menu');
        Ext.suspendLayouts();
        if(Imt.privileges.UsagePoint.canAdministrate()){
            Ext.each(actionMenuArray, function (menu) {
                menu.add(me.action);
            });
        }
        me.add(1, {
            xtype: actualForm
        });
        Ext.resumeLayouts(true);

        me.down('#edit-form').getForm().loadRecord(me.record);
        me.down('#view-form').getForm().loadRecord(me.record);
    },

    toEditMode: function (isEdit) {
        var me = this;

        Ext.suspendLayouts();
        if (isEdit) {
            me.down('#pencil-btn').hide();
            me.down('#view-form').hide();
            me.down('#edit-form').show();
            me.down('#bottom-buttons').show();
            me.action.hide();
        } else {
            me.down('#pencil-btn').show();
            me.down('#view-form').show();
            me.down('#edit-form').hide();
            me.down('#bottom-buttons').hide();
            me.action.show();
            me.down('#edit-form').getForm().clearInvalid();
        }
        Ext.resumeLayouts(true);

        me.editMode = isEdit;
        me.down('#edit-form').loadRecord(me.record);
        Imt.customattributesonvaluesobjects.service.ActionMenuManager.setAvailableEditBtns(!isEdit);
    },

    showConfirmationWindow: function(){
        var me =this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.editGeneralInformation.discard', 'IMT', 'Discard'),
            confirmation: function () {
                me.confirmationClick(confirmationWindow);
            }
        });
        confirmationWindow.show({
            width: 500,
            msg: Uni.I18n.translate('general.editGeneralInformation.lostData', 'IMT', 'You will lost unsolved data.'),
            title: Uni.I18n.translate('general.editGeneralInformation.discardChanges', 'IMT', "Discard 'General information' changes?")
        });
    },

    confirmationClick: function(confirmationWindow){
        var me = this,
            cancelBtnArray = Ext.ComponentQuery.query('inline-editable-set-property-form');

        Ext.each(cancelBtnArray, function (item) {
            if(item.editMode){
                item.model.load(item.record.get('id'),{
                    url: Ext.String.format('/api/udr/usagepoints/{0}/customproperties/', encodeURIComponent(item.parent.mRID)),
                    success: function(record){
                        item.record = record;
                        item.down('property-form').makeNotEditable(item.record);
                        Ext.suspendLayouts();
                        item.editMode = false;
                        item.down('#bottom-buttons').hide();
                        item.action.show();
                        item.down('#pencil-btn').show();
                        Ext.resumeLayouts(true);
                        Imt.customattributesonvaluesobjects.service.ActionMenuManager.setAvailableEditBtns(false);
                        me.toEditMode(true);
                        confirmationWindow.destroy();
                    }
                });
            }
        });

        var technicalAttributesPanel = Ext.ComponentQuery.query('usage-point-technical-attributes-panel')[0];
        if(technicalAttributesPanel && technicalAttributesPanel.editMode){
            technicalAttributesPanel.toEditMode(false);
            confirmationWindow.destroy();
            me.toEditMode(true);
        }
    },

    onSaveClick: function () {
        var me = this,
            form = me.down('#edit-form');
        var values = form.getValues(),
            record = me.record.copy(me.record.get('mRID'));
        record.set(values);
        me.fireEvent('saveClick', form.getForm(), record);
        //debugger;
        //this.down('#edit-form').getForm().markInvalid()
    }
});