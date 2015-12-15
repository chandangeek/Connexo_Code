package com.elster.jupiter.http.whiteboard.impl;


import com.elster.jupiter.http.whiteboard.WhiteBoard;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.ExceptionCatcher;


public class InstallerImpl {

    private final DataModel dataModel;
    private final WhiteBoard whiteBoard;

    InstallerImpl(DataModel dataModel, WhiteBoard whiteBoard) {

        this.dataModel = dataModel;
        this.whiteBoard = whiteBoard;
    }

    public void install() {
        ExceptionCatcher.executing(
                this::installDataModel
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void installDataModel() {
        dataModel.install(true, true);
    }
    public void addDefaults(){
        createMasterData();
    }

    private void createMasterData() {
        whiteBoard.createKeystore();
    }


}