Ext.define('Isu.view.workspace.issues.GroupGrid', {
    extends: 'Uni.view.container.EmptyGridContainer',
    alias: 'widget.issue-group-grid',
//    requires: [
//        'Uni.view.toolbar.PagingTop',
//        'Uni.view.toolbar.PagingBottom'
//    ],
    grid: {
        xtype: 'grid',
        name: 'groupgrid',
        hidden: true,
        store: 'Isu.store.IssuesGroups',
//        border: true,
//        columns: [
//            {
//                text: 'Reason',
//                dataIndex: 'reason',
//                flex: 5
//            },
//            {
//                text: 'Issues',
//                dataIndex: 'number',
//                flex: 1
//            }
//        ],
//        tbar: {
//            xtype: 'pagingtoolbartop',
//            store: 'Isu.store.IssuesGroups',
//            displayMsg: '{0} - {1} of {2} reasons',
//            displayMoreMsg: '{0} - {1} of more than {2} reasons',
//            emptyMsg: '0 reasons'
//        },
//        bbar: {
//            xtype: 'pagingtoolbarbottom',
//            store: 'Isu.store.IssuesGroups'
//        }
    },
    emptyComponent: {
        xtype: 'component',
        html: 'no group'
    }
});

//{
//    xtype: 'container',
//        name: 'groupinginformation',
//    margin: '20 0 0',
//    hidden: true,
//    items: [
//    {
//        xtype: 'component',
//        html: '<hr/>'
//    },
//    {
//        xtype: 'component',
//        name: 'informationtext',
//        margin: '20 0 0'
//    }
//]
//}
