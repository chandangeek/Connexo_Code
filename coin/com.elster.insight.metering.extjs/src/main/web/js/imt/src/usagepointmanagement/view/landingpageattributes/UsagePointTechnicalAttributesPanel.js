Ext.define('Imt.usagepointmanagement.view.landingpageattributes.UsagePointTechnicalAttributesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usage-point-technical-attributes-panel',

    requires: [
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormElectricity',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormWater',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormGas',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormThermal',
        'Imt.usagepointmanagement.view.SetupActionMenu'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    category: null,

    technicalAttributesConfig: {
        "ELECTRICITY": {
            form : 'technical-attributes-form-electricity',
            model: 'Imt.usagepointmanagement.model.technicalinfo.Electricity'
        },
        "GAS": {
            form : 'technical-attributes-form-gas',
            model: 'Imt.usagepointmanagement.model.technicalinfo.Gas'
        },
        "WATER": {
            form : 'technical-attributes-form-water',
            model: 'Imt.usagepointmanagement.model.technicalinfo.Water'
        },
        "THERMAL": {
            form : 'technical-attributes-form-thermal',
            model: 'Imt.usagepointmanagement.model.technicalinfo.Thermal'
        }
    },

    getTechnicalAttributesConfig: function(category){
        return this.technicalAttributesConfig[category]
    },

    initComponent: function () {
        var me = this,
            config = me.getTechnicalAttributesConfig(me.category),
            action,
            techInfoModel;

        techInfoModel = Ext.create(
            config.model,
            me.record.get('techInfo')
        );

        me.techInfo = techInfoModel;

        action = Ext.create('Ext.menu.Item',{
            itemId: 'action-menu-' + config.form,
            editAvailable: true,
            menuItemClass: 'inlineEditableAttributeSet',
//                    privileges: Imt.privileges.Device.administrateDeviceData,
            text: Uni.I18n.translate('general.editTechnicalInformation', 'IMT', "Edit 'Technical information'"),
            handler: function () {
                if(this.editAvailable){
                    me.toEditMode(true, this);
                } else {
                    console.log('cant edit');
                }
            }
        });

        me.items = [
                {
                xtype: 'title-with-edit-button',
                pencilBtnItemId: '',
                editAvailable: true,
                title: Uni.I18n.translate('general.technicalInformation', 'IMT', 'Technical information'),
                editHandler: function(){
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

        me.down('#edit-form').loadRecord(me.techInfo);
        Imt.customattributesonvaluesobjects.service.ActionMenuManager.setAvailableEditBtns(!isEdit);
    },

    onSaveClick: function () {
        var me = this,
            form = me.down('#edit-form');
        var values = form.getValues(),
            record = me.record.copy(me.record.get('mRID'));
        record.set('techInfo', values);
        me.fireEvent('saveClick', form, record);
    }
});