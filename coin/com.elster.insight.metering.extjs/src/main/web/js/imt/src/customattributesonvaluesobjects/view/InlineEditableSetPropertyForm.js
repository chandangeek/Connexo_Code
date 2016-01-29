Ext.define('Imt.customattributesonvaluesobjects.view.InlineEditableSetPropertyForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.inline-editable-set-property-form',

    router: null,
    record: null,
    actionMenuXtype: null,
    attributeSetType: null,
    labelAlign: 'top',
    layout: 'vbox',

    requires: [
        'Uni.property.form.Property',
        'Imt.util.TitleWithEditButton',
        'Imt.customattributesonvaluesobjects.service.ActionMenuManager',
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint',
        'Imt.customattributesonvaluesobjects.service.VersionsManager'
    ],

    items: [],


    initComponent: function () {
        var me = this,
            versionsContainer;
        me.items = [
            {
                xtype: 'title-with-edit-button',
                record: me.record,
                title: me.record.get('name'),
                editHandler: function () {
                    //me.down('#time-sliced-versions-container').hide();
                    me.down('property-form').makeEditable(me.record);
                    me.down('#bottom-buttons').show();
                }
            },
            {
                xtype: 'container',
                itemId: 'time-sliced-versions-container',
                layout: 'hbox',
                hidden: true,
                margin: '0 0 30 0'
            },
            {
                xtype: 'property-form',
                itemId: 'property-info-container',
                width: '100%',
                isEdit: false,
                defaults: {
                    //resetButtonHidden: true,
                    labelWidth: 250,
                    //width: 600
                }
            }
        ];

        me.dockedItems = {
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
                    itemId: 'channelCustomAttributesSaveBtn',
                    ui: 'action',
                    text: Uni.I18n.translate('general.save', 'IMT', 'Save'),
                    handler: function () {
                        me.down('property-form').updateRecord();

                        me.record.save({
                            callback: function(record, response, success){
                                if(success){
                                    me.down('property-form').makeNotEditable(me.record);
                                    me.down('#bottom-buttons').hide();
                                } else {
                                    var responseText = Ext.decode(response.response.responseText, true);
                                    if (responseText && Ext.isArray(responseText.errors)) {
                                        me.down('property-form').markInvalid(responseText.errors);
                                    }
                                }

                            }
                            //failure: function(record, response){
                            //
                            //        var responseText = Ext.decode(response.response.responseText, true);
                            //    console.log(responseText);
                            //
                            //        if (responseText && Ext.isArray(responseText.errors)) {
                            //            me.down('property-form').markInvalid(responseText.errors);
                            //
                            //        }
                            //    //me.down('property-form').markInvalid(errors);
                            //}
                        });
                    }
                },
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.restoretodefaults', 'IMT', 'Restore to defaults'),
                    icon: '../sky/build/resources/images/form/restore.png',
                    handler: function () {
                        me.down('property-form').restoreAll();
                    }
                    //itemId: 'channelCustomAttributesRestoreBtn'
                },
                {
                    xtype: 'button',
                    ui: 'link',
                    //itemId: 'channelCustomAttributesCancelBtn',
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                    handler: function () {
                        me.down('property-form').makeNotEditable(me.record);
                        me.down('#bottom-buttons').hide();
                    }
                }
            ]
        };

        me.itemId = 'CAS' + me.record.get('id');

        me.callParent(arguments);
        versionsContainer = me.down('#time-sliced-versions-container');
        if (me.record.get('timesliced')) {
            Imt.customattributesonvaluesobjects.service.VersionsManager.addVersion(me.record, versionsContainer, me.router, me.attributeSetType, me.down('#property-info-container'));
            versionsContainer.show();
        } else {
            me.down('#property-info-container').loadRecord(me.record);
        }
        if (me.actionMenuXtype && me.record.get('editable')) {
            Imt.customattributesonvaluesobjects.service.ActionMenuManager.addAction(me.actionMenuXtype, me.record, me.router, me.attributeSetType);
        }
    }
});