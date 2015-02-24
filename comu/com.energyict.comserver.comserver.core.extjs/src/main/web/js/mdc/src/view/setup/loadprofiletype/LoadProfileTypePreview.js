Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypePreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileTypePreview',
    itemId: 'loadProfileTypePreview',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu',
        'Uni.form.field.ObisDisplay',
        'Mdc.store.Intervals'
    ],
    title: '',
    frame: true,
    tools: [
        {
            xtype: 'button',
            itemId: 'load-profile-type-action-menu-button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'load-profile-type-action-menu'
            }
        }
    ],
    items: {
        xtype: 'form',
        itemId: 'load-profile-type-preview-form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
                        fieldLabel: 'Name',
                        name: 'name'
                    },
                    {
                        xtype: 'obis-displayfield',
                        name: 'obisCode'
                    },
                    {
                        fieldLabel: 'Interval',
                        name: 'timeDuration',
                        renderer: function (value) {
                            var intervalRecord = Ext.getStore('Mdc.store.Intervals').getById(value.id);
                            return intervalRecord ? intervalRecord.get('name') : '';
                        }
                    }
                ]
            },

            {
                xtype: 'fieldcontainer',
                fieldLabel: 'Register types',
                itemId: 'registerTypesArea'
            }

        ]
    },

    updateRegisterTypes: function (selectedLoadProfileType) {
        Ext.suspendLayouts();
        this.down('#registerTypesArea').removeAll();
        for (var i = 0; i < selectedLoadProfileType.get('registerTypes').length; i++) {
            var fieldlabel = i > 0 ? '&nbsp' : 'Register types',
                readingType = selectedLoadProfileType.get('registerTypes')[i].readingType;

            this.down('#registerTypesArea').add(
                {
                    xtype: 'reading-type-displayfield',
                    fieldLabel: undefined,
                    value: readingType
                }
            );
        }
        Ext.resumeLayouts(true);
    }
});


