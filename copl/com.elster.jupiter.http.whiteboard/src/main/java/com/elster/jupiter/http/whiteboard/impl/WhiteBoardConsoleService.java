package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.WhiteBoard;
import com.elster.jupiter.http.whiteboard.KeyStore;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;



@Component(name = "com.elster.jupiter.http.whiteboard.console",service = {WhiteBoardConsoleService.class}, property = {"name=" + "HTW" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=setTokenKeys","osgi.command.function=getPublicKey" }, immediate = true)

public class WhiteBoardConsoleService {

    private volatile WhiteBoard whiteBoard;
    private volatile TransactionService transactionService;


    public void setTokenKeys(){

        try (TransactionContext context = transactionService.getContext()){
            whiteBoard.createKeystore();
            context.commit();
        }
    }

    public void getPublicKey(){
        System.out.println("Public key: " + whiteBoard.getKeyPairDecrypted()!=null? whiteBoard.getKeyPairDecrypted().get("PUB"):null);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {

        this.transactionService = transactionService;
    }

    @Reference
    public void setWhiteBoard(WhiteBoard whiteBoard) {

        this.whiteBoard = whiteBoard;
    }


}