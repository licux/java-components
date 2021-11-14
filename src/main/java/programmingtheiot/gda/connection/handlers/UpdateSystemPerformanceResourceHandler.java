package programmingtheiot.gda.connection.handlers;

import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.logging.Logger;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SystemPerformanceData;

public class UpdateSystemPerformanceResourceHandler extends GenericCoapResourceHandler {

	private IDataMessageListener dataMsgListener = null;
	private static final Logger _Logger =
			Logger.getLogger(UpdateSystemPerformanceResourceHandler.class.getName());
	
	
	public UpdateSystemPerformanceResourceHandler(ResourceNameEnum resource) {
		super(resource);
		// TODO Auto-generated constructor stub
	}

	public UpdateSystemPerformanceResourceHandler(String resourceName) {
		super(resourceName);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void handleDELETE(CoapExchange context)
	{
		_Logger.info("handleDELETE is called in UpdateSystemPerformanceResourceHandler");
		context.respond(ResponseCode.DELETED);
	}
	
	@Override
	public void handleGET(CoapExchange context)
	{
		_Logger.info("handleGET is called in UpdateSystemPerformanceResourceHandler");
		context.respond(ResponseCode.CONTENT);
	}
	
	@Override
	public void handlePOST(CoapExchange context)
	{
		_Logger.info("handlePOST is called in UpdateSystemPerformanceResourceHandler");
		context.respond(ResponseCode.CHANGED);
	}
	
	@Override
	public void handlePUT(CoapExchange context)
	{
		_Logger.info("handlePUT is called in UpdateSystemPerformanceResourceHandler");
		ResponseCode code = ResponseCode.NOT_ACCEPTABLE;
		context.accept();
		
		if(this.dataMsgListener != null) {
			try {
				String jsonData = new String(context.getRequestPayload());
				SystemPerformanceData sysPerfData = DataUtil.getInstance().jsonToSystemPerformanceData(jsonData);
				
				// TODO: Choose the following (but keep it idempotent!)
				//  1) Check MID to see if it's repeated for some reason
				//   2) Cache the previous update - is the PAYLOAD repeated?
				//  3) Delegate the data check to this.dataMsgListener
				
				this.dataMsgListener.handleSystemPerformanceMessage(this.resourceName, sysPerfData);
				code = ResponseCode.CHANGED;
			}catch(Exception e) {
				_Logger.warning("Failed to handle PUT request. Message: " + e.getMessage());
				code = ResponseCode.BAD_REQUEST;
			}
		}else {
			_Logger.info("No callback listener for request. Ignoring PUT.");
			code = ResponseCode.CONTINUE;
		}
		String msg = "Update system perf date request handled: " + super.getName();
		context.respond(code, msg);
	}
	
	@Override
	public void setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null) {
			this.dataMsgListener = listener;
		}
	}
}
