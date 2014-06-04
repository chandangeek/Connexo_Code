Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeSetup',
    intervalStore: null,

//    side: [
//        {
//            xtype: 'loadProfileTypeSideFilter'
//        }
//    ],


    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            padding: '0 10 0 10',
            items: [
                {
                    xtype: 'component',
                    margins: '10 10 10 10',
                    itemId: 'loadProfileTypesTitle'
                },
//                {
//                    xtype: 'loadProfileTypeFiltering'
//                },
//                {
//                    xtype: 'menuseparator'
//                },
//                {
//                    xtype: 'loadProfileTypeSorting'
//                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileTypeDockedItemsContainer'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileTypeEmptyListContainer'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileTypeGridContainer'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileTypePreviewContainer'
                }
            ]
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
        this.down('#loadProfileTypeGridContainer').add(
            {
                xtype: 'loadProfileTypeGrid',
                intervalStore: this.intervalStore,
                store: 'LoadProfileTypes',
                editActionName: 'editloadprofiletype',
                deleteActionName: 'deleteloadprofiletype'
            }
        );
        this.down('#loadProfileTypePreviewContainer').add(
            {
                xtype: 'loadProfileTypePreview',
                intervalStore: this.intervalStore,
                editActionName: 'editloadprofiletype',
                deleteActionName: 'deleteloadprofiletype'
            }
        );
        this.down('#loadProfileTypeDockedItemsContainer').add(
            {
                xtype: 'loadProfileTypeDockedItems',
                actionHref: '#/administration/loadprofiletypes/create'
            }
        );
    }

});


