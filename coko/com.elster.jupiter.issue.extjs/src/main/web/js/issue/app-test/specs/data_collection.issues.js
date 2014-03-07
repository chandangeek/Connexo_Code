describe('Data collection issues', function () {
    var ctlr = null;

    beforeEach(function(){
        if (!ctlr) {
            ctlr = Mtr.getApplication().getController('Issues');
        }
    });

    it("tracks the number of calls", function() {
        spyOn(ctlr, 'bulkChangeIssues');
        ctlr.bulkChangeIssues();
        ctlr.bulkChangeIssues();
        return expect(ctlr.bulkChangeIssues.calls.length).toEqual(2);
    });
});