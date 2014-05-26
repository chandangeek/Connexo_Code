Ext.define('Isu.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'workspace',
    currentPath: null,

    init: function () {
        var me = this;

        crossroads.addRoute('workspace/datacollection', function () {
            me.getController('Isu.controller.DataCollectionOverview').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues', function () {
            me.getController('Isu.controller.Issues').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issuesbulkaction', function () {
            me.getController('Isu.controller.BulkChangeIssues').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}', function (id) {
            me.getController('Isu.controller.IssueDetail').showOverview(id);
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/addcomment', function (id) {
            me.getController('Isu.controller.IssueDetail').showOverview(id, true);
        });
        crossroads.addRoute('workspace/datacollection/bulkaction', function () {
            me.getController('Isu.controller.BulkChangeIssues').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/assign', function (id) {
            me.getController('Isu.controller.AssignIssues').showOverview(id);
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/close', function (id) {
            me.getController('Isu.controller.CloseIssues').showOverview(id);
        });

        this.callParent(arguments);
    }
});
