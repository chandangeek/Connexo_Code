Ext.define('Imt.usagepointmanagement.view.landingpage.UsagePointMainAttributesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usage-point-main-attributes-panel',

    requires: [
        'Imt.usagepointmanagement.view.landingpage.GeneralAttributesForm',
        'Imt.usagepointmanagement.view.landingpage.TechnicalAttributesFormElectricity'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [],
    category: null,
    record: null,

    initComponent: function () {
        var me = this,
            title = Uni.I18n.translate('general.technicalInformation', 'IMT', 'Technical information'),
            actualForm;

        switch (me.category) {
            case('ELECTRICITY'):
            {
                actualForm ='technical-attributes-form-electricity';
            }
                break;
            default:
            {
                title = Uni.I18n.translate('general.generalInformation', 'IMT', 'General information');
                actualForm ='general-attributes-form';
            }
        }

        me.items = [
            {
                xtype: 'title-with-edit-button',
                title: title,
                editHandler: function(){
                    me.down('#edit-form').loadRecord(me.record);
                    me.down('#view-form').hide();
                    me.down('#edit-form').show();
                    me.down('#bottom-buttons').show();
                    Imt.customattributesonvaluesobjects.service.ActionMenuManager.setdisabledAllEditBtns(true);
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
                            var record = me.down('#edit-form').getRecord();
                            console.log(me.down('#edit-form').getValues());
                            record.save({
                                success: function(){
                                    console.log('success');
                                },
                                failure: function(){
                                    console.log('failure');
                                }
                            })
                        }
                    },
                    {
                        xtype: 'button',
                        ui: 'link',
                        text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                        handler: function () {
                            me.down('#view-form').show();
                            me.down('#edit-form').hide();
                            me.down('#bottom-buttons').hide();
                            Imt.customattributesonvaluesobjects.service.ActionMenuManager.setdisabledAllEditBtns(false);
                        }
                    }
                ]
            }
        ];
        me.callParent();


        me.addAttributes(actualForm);
    },

    addAttributes: function(actualForm){
        var me = this;

        me.insert(1,{
            xtype: actualForm
        });
        me.down('#edit-form').getForm().loadRecord(me.record);
        me.down('#view-form').getForm().loadRecord(me.record);
    }
});