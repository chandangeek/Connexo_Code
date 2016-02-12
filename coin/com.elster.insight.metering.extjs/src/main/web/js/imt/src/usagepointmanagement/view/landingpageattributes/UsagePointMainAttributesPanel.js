Ext.define('Imt.usagepointmanagement.view.landingpageattributes.UsagePointMainAttributesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usage-point-main-attributes-panel',

    requires: [
        'Imt.usagepointmanagement.view.landingpageattributes.GeneralAttributesForm',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormElectricity',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormWater',
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
            editActionTitle = Uni.I18n.translate('general.editTechnicalInformation', 'IMT', "Edit 'Technical information'");

        if(me.category){
            actualForm = Imt.usagepointmanagement.service.AttributesMaps.getForm(me.category);
        } else {
            title = Uni.I18n.translate('general.generalInformation', 'IMT', 'General information');
            editActionTitle = Uni.I18n.translate('general.editGeneralInformation', 'IMT', "Edit 'General information'");
            actualForm = Imt.usagepointmanagement.service.AttributesMaps.getForm("GENERAL");
        }

        action = Ext.create('Ext.menu.Item',{
            itemId: 'action-menu-' + actualForm,
            menuItemClass: 'inlineEditableAttributeSet',
//                    privileges: Imt.privileges.Device.administrateDeviceData,
            text: editActionTitle,
            handler: function () {
                me.toEditMode(true, this);
            }
        });

        me.items = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'container',
                        html: '<label class="x-form-item-label x-form-item-label-top">' + title + '</label>'
                    },
                    {
                        xtype: 'button',
                        itemId: 'main-attr-pencil-btn',
                        disabled: true,
                        margin: '7 0 0 7',
                        ui: 'plain',
                        iconCls: 'icon-pencil2',
                        tooltip: Uni.I18n.translate('general.tooltip.edit', 'IMT', 'Edit'),
                    }
                ]
            },
            //{
            //    xtype: 'title-with-edit-button',
            //    pencilBtnItemId: ''
            //    title: title,
            //    editHandler: function(){
            //        me.toEditMode(true, action);
            //    }
            //},
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

                            Ext.Ajax.request({
                                url: Ext.String.format('/api/udr/usagepoints/{0}', encodeURIComponent(record.get('mRID'))),
                                method: 'PUT',
                                jsonData: Ext.encode(record.getData()),
                                timeout: 300000,
                                success: function () {
                               },
                                failure: function (response) {
                                    var responseText = Ext.decode(response.response.responseText, true);
                                    if (responseText && Ext.isArray(responseText.errors)) {
                                        me.down('#edit-form').markInvalid(responseText.errors);
                                    }
                                    record.set(baseRecord.getData());
                                }
                            });
                        }
                    },
                    {
                        xtype: 'button',
                        ui: 'link',
                        text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                        handler: function () {
                            //me.toEditMode(false, action);
                            action.show();
                            me.down('#pencil-btn').show();
                            me.down('#view-form').show();
                            me.down('#edit-form').hide();
                            me.down('#bottom-buttons').hide();
                            Imt.customattributesonvaluesobjects.service.ActionMenuManager.setDisabledAllEditBtns(false);
                            Ext.each(me.record.fields.items, function(value){
                                //console.log(value);
                                if(value.customType && value.customType == 'quantity'){
                                    me.down('#' + value.name + '-quantity').setQuantityValue(me.record.get(value.name));

                                }
                            });
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
        me.itemId = me.category ? me.category + '-attribute-set' : "GENERAL-attribute-set" ;

        Ext.suspendLayouts();

        Ext.each(actionMenuArray, function (menu) {
            menu.add(action);
        });

        me.add(1,{
            xtype: actualForm
        });

        Ext.resumeLayouts(true);

        me.down('#edit-form').getForm().loadRecord(me.record);
        me.down('#view-form').getForm().loadRecord(me.record);
    },

    toEditMode: function(isEdit, action){
        var me =this;

        Ext.suspendLayouts();
        if(isEdit){
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
        Imt.customattributesonvaluesobjects.service.ActionMenuManager.setDisabledAllEditBtns(isEdit);
    }
});