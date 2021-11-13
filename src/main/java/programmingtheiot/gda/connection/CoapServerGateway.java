/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.handlers.GenericCoapResourceHandler;
import programmingtheiot.gda.connection.handlers.GetActuatorCommandResourceHandler;
import programmingtheiot.gda.connection.handlers.UpdateSystemPerformanceResourceHandler;
import programmingtheiot.gda.connection.handlers.UpdateTelemetryResourceHandler;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class CoapServerGateway
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(CoapServerGateway.class.getName());
	
	// params
	
	private CoapServer coapServer = null;
	
	private IDataMessageListener dataMsgListener = null;
	
	
	// constructors
	
	/**
	 * Constructor.
	 * 
	 * @param dataMsgListener
	 */
	public CoapServerGateway(IDataMessageListener dataMsgListener)
	{
		super();
		
		/*
		 * Basic constructor implementation provided. Change as needed.
		 */
		
		this.dataMsgListener = dataMsgListener;
		
		initServer();
	}

		
	// public methods
	
//	public void addResource(ResourceNameEnum resource)
	public void addResource(ResourceNameEnum resourceType, String endName, Resource resource)
	{
		// TODO: while not needed for this exercise.
		// you may want to include the endName parameter as part of this resource chain creation process
		
		if(resourceType != null && resource != null) {
			createAndAddResourceChain(resourceType, resource);
		}
	}
	
	public boolean hasResource(String name)
	{
		return false;
	}
	
	public void setDataMessageListener(IDataMessageListener listener)
	{
		if(listener != null) {
			this.dataMsgListener = listener;
		}
	}
	
	public boolean startServer()
	{
		_Logger.info("CoapServerGateway start server.");
		this.coapServer.start();
		return true;
	}
	
	public boolean stopServer()
	{
		_Logger.info("CoapServerGateway stop server.");
		this.coapServer.stop();
		return true;
	}
	
	
	// private methods
	
	private void createAndAddResourceChain(ResourceNameEnum resourceType, Resource resource) {
		_Logger.info("Adding server resource handler chain:" + resourceType.getResourceName());
		
		List<String> resourceNames = resourceType.getResourceNameChain();
		Queue<String> queue = new ArrayBlockingQueue<>(resourceNames.size());
		
		queue.addAll(resourceNames);
		Resource parentResource = this.coapServer.getRoot();
		
		if(parentResource == null) {
			parentResource = new CoapResource(queue.poll());
			this.coapServer.add(parentResource);
		}
		
		while(!queue.isEmpty()) {
			String resourceName = queue.poll();
			Resource nextResource = parentResource.getChild(resourceName);
			
			if(nextResource == null) {
				if(queue.isEmpty()) {
					nextResource = resource;
					nextResource.setName(resourceName);
				}else {
					nextResource = new CoapResource(resourceName);
				}
				parentResource.add(nextResource);
			}
			
			parentResource = nextResource;
		}
	}
	
	private Resource createResourceChain(ResourceNameEnum resourceType)
	{
		return null;
	}
	
	private void initServer(ResourceNameEnum ...resources)
	{
		this.coapServer = new CoapServer();
		
		GetActuatorCommandResourceHandler getActuatorCmdResourceHandler 
		 = new GetActuatorCommandResourceHandler(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE);
		
		if(this.dataMsgListener != null) {
			this.dataMsgListener.setActuatorDataListener(null, getActuatorCmdResourceHandler);
		}
		
		addResource(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, null, getActuatorCmdResourceHandler);
		
		UpdateSystemPerformanceResourceHandler updateSysPerfResourceHandler = new UpdateSystemPerformanceResourceHandler(
				ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);
		updateSysPerfResourceHandler.setDataMessageListener(this.dataMsgListener);
		addResource(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, null, updateSysPerfResourceHandler);
		
		UpdateTelemetryResourceHandler updateTelemetryResourceHandler = new UpdateTelemetryResourceHandler(
				ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
		updateTelemetryResourceHandler.setDataMessageListener(this.dataMsgListener);
		addResource(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, null, updateTelemetryResourceHandler);
		
	}
}
