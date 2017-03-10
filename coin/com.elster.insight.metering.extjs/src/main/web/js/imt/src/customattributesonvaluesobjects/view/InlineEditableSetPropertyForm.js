/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.view.InlineEditableSetPropertyForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.inline-editable-set-property-form',

    canAdministrate: true,
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
                hiddenBtn: !me.canAdministrate || (!me.record.get('isEditable') || (me.record.get('isVersioned') && !me.record.get('isActive'))),
                editHandler: function () {
                    if(this.editAvailable){
                        me.toEditMode(true);
                    } else {
                        me.showConfirmationWindow();
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
                    me.router.getRoute('usagepoints/view/history').forward(null, {customAttributeSetId: me.record.getId(), selectCurrent: true});
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
                    iconCls: 'icon-rotate-ccw3',
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
                                url: Ext.String.format('/api/udr/usagepoints/{0}/customproperties/', encodeURIComponent(me.parent.name)),
                                success: function(record){
                                    me.record = record;
                                    me.toEditMode(false);
                                }
                            });
                        }
                    }
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
        if (me.canAdministrate && (me.actionMenuXtype && me.record.get('isEditable'))) {

            me.action = Ext.create('Ext.menu.Item', {
                itemId: 'action-menu-custom-attribute' + me.record.get('id'),
                menuItemClass: 'inlineEditableAttributeSet',
                editAvailable: true,
                text: Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", [Ext.String.htmlEncode(me.record.get('name'))]),
                handler: function () {
                    if(this.editAvailable){
                        me.toEditMode(true);
                    } else {
                        me.showConfirmationWindow();
                    }
                }
            });

            Ext.suspendLayouts();
            if (!(me.record.get('isVersioned') && !me.record.get('isActive'))) {
                Ext.each(actionMenusArray, function (menu) {
                    menu.add(me.action);
                });
            }
            Ext.resumeLayouts(true);
        }
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
            msg: Uni.I18n.translate('general.editGeneralInformation.lostData', 'IMT', 'Unsaved changes will be lost.'),
            title: Uni.I18n.translate('general.editGeneralInformation.discardChanges', 'IMT', "Discard '{0}' changes?",[Ext.String.htmlEncode(me.record.get('name'))])
        });
    },

    confirmationClick: function(confirmationWindow){
        var me = this,
            cancelBtnArray = Ext.ComponentQuery.query('inline-editable-set-property-form');

        Ext.each(cancelBtnArray, function (item) {
            if(item.editMode){
                item.model.load(item.record.get('id'),{
                    url: Ext.String.format('/api/udr/usagepoints/{0}/customproperties/', encodeURIComponent(item.parent.name)),
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
        var generalAttributesPanel = Ext.ComponentQuery.query('usage-point-main-attributes-panel')[0];
        if(generalAttributesPanel && generalAttributesPanel.editMode){
            generalAttributesPanel.toEditMode(false);
            confirmationWindow.destroy();
            me.toEditMode(true);
        }
    },

    toEditMode: function(isEdit){
        var me = this;
        if(isEdit){
            me.down('property-form').makeEditable(me.record);
            Ext.suspendLayouts();
            me.down('#bottom-buttons').show();
            me.action.hide();
            me.down('#pencil-btn').hide();
            Ext.resumeLayouts(true);
        } else {
            me.down('property-form').makeNotEditable(me.record);
            Ext.suspendLayouts();
            me.down('#bottom-buttons').hide();
            me.action.show();
            me.down('#pencil-btn').show();
            Ext.resumeLayouts(true);
        }
        me.editMode = isEdit;
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