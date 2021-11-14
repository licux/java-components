package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SystemPerformanceData;

public class GetActuatorCommandResourceHandler extends GenericCoapResourceHandler implements IActuatorDataListener{
	
	private ActuatorData actuatorData = null;
	
	private static final Logger _Logger =
			Logger.getLogger(GetActuatorCommandResourceHandler.class.getName());
	
	
	public GetActuatorCommandResourceHandler(ResourceNameEnum resource) {
		super(resource);
		super.setObservable(true);
		
		// For Debug purpose
		this.actuatorData = new ActuatorData();
		this.actuatorData.setLatitude(1.0f);
		this.actuatorData.setLocationID("test location");
		this.actuatorData.setLongitude(5.5f);
	}

	public GetActuatorCommandResourceHandler(String resourceName) {
		super(resourceName);
		super.setObservable(true);
	}

	@Override
	public void handleDELETE(CoapExchange context)
	{
		_Logger.info("handleDELETE is called in GetActuatorCommandResourceHandler");
		context.respond(ResponseCode.NOT_ACCEPTABLE);
	}
	
	@Override
	public void handleGET(CoapExchange context)
	{
		_Logger.info("handleGET is called in GetActuatorCommandResourceHandler");
		String jsonData = "";
		ResponseCode code =ResponseCode.NOT_ACCEPTABLE;
		
		context.accept();
		
		// TODO: validate the request
		
		try {
			jsonData = DataUtil.getInstance().actuatorDataToJson(this.actuatorData);
			code = ResponseCode.CONTENT;
		}catch(Exception e) {
			_Logger.warning("Failed to handle PUT request. Message: " + e.getMessage());
			code = ResponseCode.INTERNAL_SERVER_ERROR;
		}
		context.respond(code, jsonData);
	}
	
	@Override
	public void handlePOST(CoapExchange context)
	{
		_Logger.info("handlePOST is called in GetActuatorCommandResourceHandler");
		context.respond(ResponseCode.NOT_ACCEPTABLE);
	}
	
	@Override
	public void handlePUT(CoapExchange context)
	{
		_Logger.info("handlePUT is called in GetActuatorCommandResourceHandler");
		context.respond(ResponseCode.NOT_ACCEPTABLE);
	}
	
	public void setDataMessageListener(IDataMessageListener listener)
	{
	}

	@Override
	public boolean onActuatorDataUpdate(ActuatorData data) {
		if(data != null) {
			if(this.actuatorData == null) {
				this.actuatorData = new ActuatorData();
			}
			this.actuatorData.updateData(data);
			
			super.changed();
			_Logger.fine("Actuator data updated for URI: " +super.getURI() + ": Data value = " + this.actuatorData.getValue());
			
			return true;
		}
		return false;
	}
}
