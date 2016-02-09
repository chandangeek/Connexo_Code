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
                hiddenBtn: !me.record.get('isEditable') || (me.record.get('isVersioned') && !me.record.get('isActive')),
                editHandler: function () {
                    Imt.customattributesonvaluesobjects.service.ActionMenuManager.setDisabledAllEditBtns(true);
                    me.down('property-form').makeEditable(me.record);
                    me.down('#bottom-buttons').show();
                    action.hide();
                    me.down('#pencil-btn').hide();
                }
            },
            {
                xtype: 'container',
                itemId: 'time-sliced-versions-container',
                layout: 'hbox',
                hidden: true,
                margin: '0 0 10 0'
            },
            {
                xtype: 'property-form',
                itemId: 'property-info-container',
                width: '100%',
                isEdit: false,
                defaults: {
                    labelWidth: 250
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
                        me.record.set('parent', me.parent);

                        me.record.save({
                            success: function (record, response, success) {

                                me.router.getRoute().forward();

                            },
                            failure: function (record, response, success) {
                                var responseText = Ext.decode(response.response.responseText, true);
                                console.log(responseText);
                                if (responseText && Ext.isArray(responseText.errors)) {
                                    me.down('property-form').markInvalid(responseText.errors);
                                }
                            }
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
                },
                {
                    xtype: 'button',
                    ui: 'link',
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                    handler: function () {
                        me.down('property-form').restoreAll();
                        me.down('property-form').updateRecord();

                        Imt.customattributesonvaluesobjects.service.ActionMenuManager.setDisabledAllEditBtns(false);
                        me.down('property-form').makeNotEditable(me.record);
                        me.down('#bottom-buttons').hide();
                        action.show();
                        me.down('#pencil-btn').show();
                    }
                }
            ]
        };

        me.itemId = 'CAS' + me.record.get('id');

        me.callParent(arguments);
        versionsContainer = me.down('#time-sliced-versions-container');
        if (me.record.get('isVersioned')) {
            Imt.customattributesonvaluesobjects.service.VersionsManager.addVersion(me.record, versionsContainer, me.router, me.attributeSetType, me.down('#property-info-container'), true);
            versionsContainer.show();
        } else {
            me.down('#property-info-container').loadRecord(me.record);
        }
        if (me.actionMenuXtype && me.record.get('isEditable')) {

            action = Ext.create('Ext.menu.Item', {
                itemId: 'action-menu-custom-attribute' + me.record.get('id'),
                menuItemClass: 'inlineEditableAttributeSet',
                text: Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", [Ext.String.htmlEncode(me.record.get('name'))]),
                handler: function () {
                    Imt.customattributesonvaluesobjects.service.ActionMenuManager.setDisabledAllEditBtns(true);
                    me.down('property-form').makeEditable(me.record);
                    me.down('#bottom-buttons').show();
                    me.down('#pencil-btn').hide();
                    this.hide();
                }
            });

            if (!(me.record.get('isVersioned') && !me.record.get('isActive'))) {
                Ext.each(actionMenusArray, function (menu) {
                    menu.add(action);
                });
            }
        }
    }
});