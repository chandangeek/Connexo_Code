Ext.define('Imt.usagepointmanagement.view.landingpageattributes.UsagePointMainAttributesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usage-point-main-attributes-panel',

    requires: [
        'Imt.usagepointmanagement.view.landingpageattributes.GeneralAttributesForm',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormElectricity',
        'Imt.usagepointmanagement.view.SetupActionMenu'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [],
    category: null,
    record: null,

    initComponent: function () {
        var me = this, action,
            title = Uni.I18n.translate('general.technicalInformation', 'IMT', 'Technical information'),
            actualForm,
            editActionTitle = Uni.I18n.translate('general.editTechnicalInformation', 'IMT', 'Edit Technical information');

        if(me.category){
            actualForm = Imt.usagepointmanagement.service.AttributesMaps.getForm(me.category);
        } else {
            title = Uni.I18n.translate('general.generalInformation', 'IMT', 'General information');
            editActionTitle = Uni.I18n.translate('general.editGeneralInformation', 'IMT', 'Edit General information');
            actualForm = Imt.usagepointmanagement.service.AttributesMaps.getForm("GENERAL");
        }

        action = Ext.create('Ext.menu.Item',{
            itemId: 'action-menu-' + actualForm,
            menuItemClass: 'inlineEditableAttributeSet',
//                    privileges: Imt.privileges.Device.administrateDeviceData,
            text: editActionTitle,
            handler: function () {
                me.down('#edit-form').loadRecord(me.record);
                me.down('#pencil-btn').hide();
                me.down('#view-form').hide();
                me.down('#edit-form').show();
                me.down('#bottom-buttons').show();
                Imt.customattributesonvaluesobjects.service.ActionMenuManager.setDisabledAllEditBtns(true);
                this.hide();
            }
        });

        me.items = [
            {
                xtype: 'title-with-edit-button',
                title: title,
                editHandler: function(){
                    action.hide();
                    me.down('#pencil-btn').hide();
                    me.down('#edit-form').loadRecord(me.record);
                    me.down('#view-form').hide();
                    me.down('#edit-form').show();
                    me.down('#bottom-buttons').show();
                    Imt.customattributesonvaluesobjects.service.ActionMenuManager.setDisabledAllEditBtns(true);
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
                        handler: function(){
                            var  record = me.down('#edit-form').getRecord();
                            var baseRecord = record.copy();

                            record.set(me.down('#edit-form').getValues());

                            record.save({
                                success: function(){

                                },
                                failure: function(record, response, success){
                                    var responseText = Ext.decode(response.response.responseText, true);
                                    if (responseText && Ext.isArray(responseText.errors)) {
                                        me.down('#edit-form').markInvalid(responseText.errors);
                                    }
                                    record.set(baseRecord.getData());
                                }
                            })
                        }
                    },
                    {
                        xtype: 'button',
                        ui: 'link',
                        text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                        handler: function () {
                            action.show();
                            me.down('#pencil-btn').show();
                            me.down('#view-form').show();
                            me.down('#edit-form').hide();
                            me.down('#bottom-buttons').hide();
                            Imt.customattributesonvaluesobjects.service.ActionMenuManager.setDisabledAllEditBtns(false);
                        }
                    }
                ]
            }
        ];
        me.callParent();

        me.addAttributes(actualForm, action);
    },

    addAttributes: function(actualForm, action){
        var me = this,
            actionMenuArray=Ext.ComponentQuery.query('usage-point-setup-action-menu');

        Ext.each(actionMenuArray, function (menu) {
            menu.add(action);
        });

        me.add(1,{
            xtype: actualForm
        });

        me.down('#edit-form').getForm().loadRecord(me.record);
        me.down('#view-form').getForm().loadRecord(me.record);
    }
});