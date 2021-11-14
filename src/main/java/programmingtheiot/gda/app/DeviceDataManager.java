/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.SystemStateData;
import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.IRequestResponseClient;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;
import programmingtheiot.gda.connection.SmtpClientConnector;
import programmingtheiot.gda.system.SystemPerformanceManager;

/**
 * Shell representation of class for student implementation.
 *
 */
public class DeviceDataManager implements IDataMessageListener
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(DeviceDataManager.class.getName());
	
	// private var's
	
	private boolean enableMqttClient = true;
	private boolean enableCoapServer = false;
	private boolean enableCloudClient = false;
	private boolean enableSmtpClient = false;
	private boolean enablePersistenceClient = false;
	
	private IActuatorDataListener actuatorDataListener = null;
	private IPubSubClient mqttClient = null;
	private IPubSubClient cloudClient = null;
	private IPersistenceClient persistenceClient = null;
	private IRequestResponseClient smtpClient = null;
	private CoapServerGateway coapServer = null;
	
	private SystemPerformanceManager sysPerfMgr = null;
	
	// constructors
	
	public DeviceDataManager()
	{
		super();
		
		ConfigUtil configUtil = ConfigUtil.getInstance();
		this.enableMqttClient  = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);
		this.enableCoapServer  = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);
		this.enableCloudClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
		this.enableSmtpClient  = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_SMTP_CLIENT_KEY);
		this.enablePersistenceClient  = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);
		
		initConnections();
	}
	
	public DeviceDataManager(
		boolean enableMqttClient,
		boolean enableCoapClient,
		boolean enableCloudClient,
		boolean enableSmtpClient,
		boolean enablePersistenceClient)
	{
		super();
		
		this.enableMqttClient = enableMqttClient;
		this.enableCoapServer = enableCoapClient;
		this.enableCloudClient = enableCloudClient;
		this.enableSmtpClient = enableSmtpClient;
		this.enablePersistenceClient = enablePersistenceClient;
		
		initConnections();
	}
	
	
	// public methods
	
	@Override
	public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
	{
		if(data == null) {
			_Logger.warning("Response ActuatorData is null");
			return false;
		}
		_Logger.info("Response ActuatorData was received from CDA.");
		if(!data.isResponseFlagEnabled() || data.hasError()) {
			_Logger.warning("Received ActuatorData has Error!");
		}
		if(this.actuatorDataListener != null) {
			this.actuatorDataListener.onActuatorDataUpdate(data);
		}
		return true;
	}

	@Override
	public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)
	{
		if(msg == null || msg.equals("")) {
			_Logger.warning("Invalid Json msg was received.");
		}
		_Logger.info("JSON Data was received from cloud.");
		try {
			ActuatorData aData = DataUtil.getInstance().jsonToActuatorData(msg);
			return true;
		}catch(Exception e) {
			
		}
		try {
			SystemStateData sData = DataUtil.getInstance().jsonToSystemStateData(msg);
			return true;
		}catch(Exception e) {
			
		}
		
		return false;
	}

	@Override
	public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
	{
		if(data == null) {
			_Logger.warning("Response SensorData is null");
			return false;
		}
		_Logger.info("Response SensorData was received from CDA.");
		if(data.hasError()) {
			_Logger.warning("Received SensorData has Error!");
		}
		return true;
	}

	@Override
	public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
	{
		if(data == null) {
			_Logger.warning("Response SystemPerformanceData is null");
			return false;
		}
		_Logger.info("Response SystemPerformanceData was received from CDA.");
		if(data.hasError()) {
			_Logger.warning("Received SystemPerformanceData has Error!");
		}
		return true;
	}
	
	public void setActuatorDataListener(String name, IActuatorDataListener listener)
	{
		if(listener != null) {
			this.actuatorDataListener = listener;
		}
	}
	
	public void startManager()
	{
		_Logger.info("Starting DeviceDataManager...");
		this.sysPerfMgr.startManager();
		
		if(this.enableMqttClient) {
			try {
				int qos = ConfigConst.DEFAULT_QOS;
				if(this.mqttClient.connectClient()) {
					this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
					this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
					this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
					_Logger.info("MQTT client connection started.");
				}else {
					_Logger.warning("MQTT client conection start failed.");
				}
			}catch(Exception e) {
				_Logger.warning("Failed to start MQTT client");
			}
		}
		if(this.enableCoapServer) {
			this.coapServer.startServer();
		}
		if(this.enableCloudClient) {
//			this.cloudClient.connectClient();
		}
		if(this.enableSmtpClient) {
			// stateless client connection, so do nothing 
		}
		if(this.enablePersistenceClient) {
//			this.persistenceClient.connectClient();
		}
	}
	
	public void stopManager()
	{
		_Logger.info("Stopping DeviceDataManager...");
		this.sysPerfMgr.stopManager();
		
		if(this.enableMqttClient) {
			try {
				if(this.mqttClient.disconnectClient()) {
					this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
					this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
					this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);
					_Logger.info("Stopped MQTT client");
				}else {
					_Logger.info("Failed to stop MQTT client");
				}
			}catch(Exception e) {
				_Logger.warning("Failed to stop MQTT client");
			}
			this.mqttClient.disconnectClient();
		}
		if(this.enableCoapServer) {
			this.coapServer.stopServer();	
		}
		if(this.enableCloudClient) {
//			this.cloudClient.disconnectClient();
		}
		if(this.enableSmtpClient) {
			// stateless client connection, so do nothing 
		}
		if(this.enablePersistenceClient) {
//			this.persistenceClient.disconnectClient();
		}
	}

	
	// private methods
	
	/**
	 * Initializes the enabled connections. This will NOT start them, but only create the
	 * instances that will be used in the {@link #startManager() and #stopManager()) methods.
	 * 
	 */
	private void initConnections()
	{
		this.sysPerfMgr = new SystemPerformanceManager();
		this.sysPerfMgr.setDataMessageListener(this);

		if(this.enableMqttClient) {
			this.mqttClient = new MqttClientConnector();
			this.mqttClient.setDataMessageListener(this);
		}
		if(this.enableCoapServer) {
			this.coapServer = new CoapServerGateway(this);
//			this.coapServer.setDataMessageListener(this);
		}
		if(this.enableCloudClient) {
			// Todo
		}
		if(this.enableSmtpClient) {
			// Todo
		}
		if(this.enablePersistenceClient) {
			// Todo
		}
	}
	
}
