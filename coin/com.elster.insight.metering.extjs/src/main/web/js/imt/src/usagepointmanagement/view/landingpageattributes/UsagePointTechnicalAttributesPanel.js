Ext.define('Imt.usagepointmanagement.view.landingpageattributes.UsagePointTechnicalAttributesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usage-point-technical-attributes-panel',

    requires: [
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormElectricity',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormWater',
        'Imt.usagepointmanagement.view.SetupActionMenu'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    category: null,
    //record: null,

    initComponent: function () {
        var me = this,
            config = Imt.usagepointmanagement.service.AttributesMaps.getTechnicalAttributesConfig(me.category),
            action,
            techInfoModel;

        techInfoModel = Ext.create(
            config.model,
            me.record.get('techInfo')
        );

        action = Ext.create('Ext.menu.Item',{
            itemId: 'action-menu-' + config.form,
            menuItemClass: 'inlineEditableAttributeSet',
//                    privileges: Imt.privileges.Device.administrateDeviceData,
            text: Uni.I18n.translate('general.editTechnicalInformation', 'IMT', "Edit 'Technical information'"),
            handler: function () {
                me.toEditMode(true, this);
            }
        });

        me.items = [
                {
                xtype: 'title-with-edit-button',
                pencilBtnItemId: '',
                title: Uni.I18n.translate('general.technicalInformation', 'IMT', 'Technical information'),
                editHandler: function(){
                    me.toEditMode(true, action);
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
                        //handler: function(){
                        //    var  record = me.down('#edit-form').getRecord();
                        //    var baseRecord = record.copy();
                        //
                        //    record.set(me.down('#edit-form').getValues());
                        //
                        //    Ext.Ajax.request({
                        //        url: Ext.String.format('/api/udr/usagepoints/{0}', encodeURIComponent(record.get('mRID'))),
                        //        method: 'PUT',
                        //        jsonData: Ext.encode(record.getData()),
                        //        timeout: 300000,
                        //        success: function () {
                        //       },
                        //        failure: function (response) {
                        //            var responseText = Ext.decode(response.responseText, true);
                        //            if (responseText && Ext.isArray(responseText.errors)) {
                        //                me.down('#edit-form').markInvalid(responseText.errors);
                        //            }
                        //            record.set(baseRecord.getData());
                        //        }
                        //    });
                        //}
                    },
                    {
                        xtype: 'button',
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

        me.addAttributes(config.form, techInfoModel, action);
    },

    addAttributes: function(form, record, action){
        var me = this,
            actionMenuArray=Ext.ComponentQuery.query('usage-point-setup-action-menu');
        me.itemId =  me.category + '-attribute-set';

        Ext.suspendLayouts();

        Ext.each(actionMenuArray, function (menu) {
            menu.add(action);
        });

        me.add(1,{
            xtype: form
        });

        Ext.resumeLayouts(true);

        me.down('#edit-form').getForm().loadRecord(record);
        me.down('#view-form').getForm().loadRecord(record);
    },

    toEditMode: function(isEdit, action){
        var me =this;

        Ext.suspendLayouts();
        if(isEdit){
            //if(me.category){
            //    Ext.each(me.record.fields.items, function(value){
            //        if(value.customType && value.customType == 'quantity'){
            //            me.record.get(value.name) && me.down('#' + value.name + '-quantity').setQuantityValue(me.record.get(value.name));
            //        }
            //    });
            //}
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

        //me.down('#edit-form').loadRecord(actualModel);
        Imt.customattributesonvaluesobjects.service.ActionMenuManager.setDisabledAllEditBtns(isEdit);
    },

    onSaveClick: function () {
        var me = this,
            form = me.down('#edit-form');
        var  techInfo = form.getRecord();

        me.record.set('techInfo', techInfo);
        me.fireEvent('saveClick', form, me.record);
    }
});