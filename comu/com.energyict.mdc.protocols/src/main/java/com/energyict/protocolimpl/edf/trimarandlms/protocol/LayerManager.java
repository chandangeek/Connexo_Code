/*
 * LayerManager.java
 *
 * Created on 15 februari 2007, 11:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LayerManager {

    final int DEBUG=0;

    Connection62056 connection;
    int sourceTransportAddress;
    int destinationTransportAddress;

    /** Creates a new instance of LayerManager */
    public LayerManager(Connection62056 connection) {
        this.connection=connection;
    }

    public byte[] send(byte[] applicationData) throws IOException {
        int retryRespond=0;
        int retryTimeout=0;
        int retryRequest=0;
        while(true) {
            try {
                TSDU tsdu = new TSDU();
                tsdu.init(applicationData);
                if (DEBUG>=1) {
					System.out.println("KV_DEBUG> ********************** LayerManager, request");
				}
                connection.getTransport6205651().request(false,sourceTransportAddress,destinationTransportAddress,tsdu);
            }
            catch(ConnectionException e) {
                if (e.getReason() == connection.getPROTOCOL_ERROR()) {
                    if (retryRequest++>=5){
                        throw new IOException(e.toString()+", max retries!");
                    } else {
						continue;
					}
                } else {
					throw e;
				}
            }

            while(true) {
                try {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> ********************** LayerManager, respond");
					}
                    return connection.getTransport6205651().respond().getData();
                }
                catch(ConnectionException e) {
                    if (e.getReason() == connection.getPROTOCOL_ERROR()) {
                        if (retryRespond++>=50) {
							throw new IOException(e.toString()+", max retries "+retryRespond+"!");
						}
                    }
                    else if (e.getReason() == connection.getTIMEOUT_ERROR()) {
                        if (retryTimeout++>=5) {
							throw new IOException(e.toString()+", max retries "+retryTimeout+"!");
						} else {
							break; // retry with send!
						}
                    } else {
						throw e;
					}
                }
            } // while(true)


        } // while(true)
    } // public byte[] send(byte[] applicationData) throws IOException {

    public void init(int sourceTransportAddress, int destinationTransportAddress) {
        this.sourceTransportAddress=sourceTransportAddress;
        this.destinationTransportAddress=destinationTransportAddress;
    }
}
