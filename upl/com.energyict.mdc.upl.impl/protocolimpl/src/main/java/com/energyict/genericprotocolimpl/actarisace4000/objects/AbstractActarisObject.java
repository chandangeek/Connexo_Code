/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

/**
 * @author gna
 *
 */
abstract public class AbstractActarisObject {
	
	private ObjectFactory objectFactory;
	
	abstract protected int getTrackingID();
	abstract protected void setTrackingID(int trackingID);

	/**
	 * @param ObjectFactory of
	 */
	public AbstractActarisObject(ObjectFactory of) {
		this.objectFactory = of;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}
	
	public void request(String msg){
		getObjectFactory().getAace().getPacket().sendMessage(msg);
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

}
