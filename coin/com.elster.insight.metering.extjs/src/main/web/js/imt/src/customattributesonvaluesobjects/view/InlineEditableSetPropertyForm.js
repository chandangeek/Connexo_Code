Ext.define('Imt.customattributesonvaluesobjects.view.InlineEditableSetPropertyForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.inline-editable-set-property-form',

    parent: null,
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
            versionsContainer,
            action,
            actionMenusArray = Ext.ComponentQuery.query(me.actionMenuXtype);

        me.items = [
            {
                xtype: 'title-with-edit-button',
                record: me.record,
                title: me.record.get('name'),
                editAvailable: true,
                hiddenBtn: !me.record.get('isEditable') || (me.record.get('isVersioned') && !me.record.get('isActive')),
                editHandler: function () {

                    if(this.editAvailable){
                        me.toEditMode(true,action);
                    } else {
                        console.log('cant edit');
                    }

                }
            },
            {
                xtype: 'container',
                itemId: 'time-sliced-versions-container',
                layout: 'hbox',
                hidden: true
            },
            {
                xtype: 'button',
                itemId: 'time-sliced-versions-button',
                ui: 'link',
                text: Uni.I18n.translate('customattributesets.versions', 'IMT', 'Versions'),
                hidden: true,
                handler: function() {
                    me.router.getRoute('usagepoints/view/history').forward(null, {customAttributeSetId: me.record.getId()});
                },
                margin: '-5 0 0 -10'

            },
            {
                xtype: 'property-form',
                itemId: 'property-info-container',
                width: '100%',
                isEdit: false,
                defaults: {
                    labelWidth: 250,
                    resetButtonHidden: false,
                    minHeight: 35
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
                    itemId: 'inlineCustomAttributesSaveBtn',
                    ui: 'action',
                    text: Uni.I18n.translate('general.save', 'IMT', 'Save'),
                    record: me.record,
                    listeners: {
                        click: me.onSaveClick,
                        scope: me
                    }
                },
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.restoretodefaults', 'IMT', 'Restore to defaults'),
                    iconCls: 'icon-spinner12',
                    handler: function () {
                        me.down('property-form').restoreAll();
                    }
                },
                {
                    xtype: 'button',
                    ui: 'link',
                    itemId: 'edit-attributes-cancel-btn',
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                    listeners: {
                        click: function(){
                            me.model.load(me.record.get('id'),{
                                url: Ext.String.format('/api/udr/usagepoints/{0}/customproperties/', encodeURIComponent(me.parent.mRID)),
                                success: function(record){
                                    me.record = record;
                                    me.toEditMode(false,action);
                                }
                            });
                        }

                    },
                    //handler: function () {
                    //    me.model.load(me.record.get('id'),{
                    //        url: Ext.String.format('/api/udr/usagepoints/{0}/customproperties/', encodeURIComponent(me.parent.mRID)),
                    //        success: function(record){
                    //            me.record = record;
                    //            me.toEditMode(false,action);
                    //        }
                    //    });
                    //}
                }
            ]
        };

        me.itemId = me.record.get('name') + '-attribute-set';

        me.callParent(arguments);
        versionsContainer = me.down('#time-sliced-versions-container');
        if (me.record.get('isVersioned')) {
            Imt.customattributesonvaluesobjects.service.VersionsManager.addVersion(me.record, versionsContainer, me.router, me.attributeSetType, me.down('#property-info-container'), true);
            versionsContainer.show();
            me.down('#time-sliced-versions-button').show();
        } else {
            me.down('#property-info-container').loadRecord(me.record);
        }
        if (me.actionMenuXtype && me.record.get('isEditable')) {

            action = Ext.create('Ext.menu.Item', {
                itemId: 'action-menu-custom-attribute' + me.record.get('id'),
                menuItemClass: 'inlineEditableAttributeSet',
                editAvailable: true,
                text: Uni.I18n.translate('general.edit', 'IMT', "Edit '{0}'", [Ext.String.htmlEncode(me.record.get('name'))]),
                handler: function () {
                    if(this.editAvailable){
                        me.toEditMode(true,this);
                    } else {
                        console.log('cant edit');
                    }
                }
            });

            Ext.suspendLayouts();
            if (!(me.record.get('isVersioned') && !me.record.get('isActive'))) {
                Ext.each(actionMenusArray, function (menu) {
                    menu.add(action);
                });
            }
            Ext.resumeLayouts(true);
        }
    },

    toEditMode: function(isEdit, action){
        var me = this;
        if(isEdit){
            me.down('property-form').makeEditable(me.record);
            Ext.suspendLayouts();
            me.down('#bottom-buttons').show();
            action.hide();
            me.down('#pencil-btn').hide();
            Ext.resumeLayouts(true);
        } else {
            me.down('property-form').makeNotEditable(me.record);
            Ext.suspendLayouts();
            me.down('#bottom-buttons').hide();
            action.show();
            me.down('#pencil-btn').show();
            Ext.resumeLayouts(true);
        }

        Imt.customattributesonvaluesobjects.service.ActionMenuManager.setAvailableEditBtns(!isEdit);

    },

    onSaveClick: function () {
        var me =this,
            form = this.down('property-form');
        form.updateRecord();
        me.record.set('parent', me.parent);
        me.fireEvent('saveClick', form, this.record);
    }
});