/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(MqttClientConnector.class.getName());
	
	// params
	private MqttClient mqttClient = null;
	private IDataMessageListener dataMsgListener = null;
	
	private String protocol = null;
	private String host = null;
	private int port = -1;
	private int brokerKeepAlive = -1;
	
	private String clientID = null;
	private MemoryPersistence persistence = null;
	private MqttConnectOptions connOpts = null;
	private String brokerAddr = null;
	
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public MqttClientConnector()
	{
		super();
		ConfigUtil configUtil = ConfigUtil.getInstance();
		
		this.protocol = "tcp";
		this.host = configUtil.getProperty(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);
		this.port = configUtil.getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT);
		this.brokerKeepAlive = configUtil.getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);
		// paho Java client requires a client ID
		this.clientID = MqttClient.generateClientId();
		// these are specific to the MQTT connection which will be used during connect
		this.persistence = new MemoryPersistence();
		this.connOpts = new MqttConnectOptions();
		this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
		this.connOpts.setCleanSession(false);
		this.connOpts.setAutomaticReconnect(true);
		
		this.brokerAddr = this.protocol + "://" + this.host + ":" + this.port;
		
	}
	
	
	// public methods
	
	@Override
	public boolean connectClient()
	{
		if(this.mqttClient == null) {
			try {
				this.mqttClient = new MqttClient(this.brokerAddr, this.clientID, this.persistence);
				this.mqttClient.setCallback(this);
				if(!this.mqttClient.isConnected()) {
					this.mqttClient.connect(this.connOpts);
				}
			} catch (MqttException e) {
				_Logger.warning("MqttClient Connect Error! Exception = MqttException");
				e.printStackTrace();
				return false;
			}
		}else {
			_Logger.info("Mqtt client was already connected.");
			return false;
		}
		_Logger.info("MqttClientConnector connectd successfully!");
		return true;
	}

	@Override
	public boolean disconnectClient()
	{
		if(this.mqttClient.isConnected()) {
			try {
				this.mqttClient.disconnect();
			} catch (MqttException e) {
				_Logger.warning("MqttClient Disconnect Error! Exception = MqttException");
				e.printStackTrace();
				return false;
			}
		}else {
			_Logger.info("Mqtt client was already disconnected.");
			return false;
		}
		_Logger.info("MqttClientConnector disconnectd successfully!");
		return true;
	}

	public boolean isConnected()
	{
		return (this.mqttClient != null && mqttClient.isConnected());
	}
	
	@Override
	public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos)
	{
		_Logger.info("MqttClientConnector%publishMessage: topicName[" + topicName + "], message[" + msg +"]");
		if(qos < 0 || qos > 2) {
			qos = ConfigConst.DEFAULT_QOS;
		}
		if(isConnected()) {
			try {
				this.mqttClient.publish(topicName.getResourceName(), msg.getBytes(), qos, false);
				return true;
			}catch(Exception e) {
				_Logger.warning("Failed to publish MQTT message:" + e.getMessage());
			}
		}else {
			_Logger.warning("No connection to broker. Ignoring publish. Broker/topic: " + this.mqttClient.getCurrentServerURI() + topicName.getResourceName());
		}
		return false;
	}

	@Override
	public boolean subscribeToTopic(ResourceNameEnum topicName, int qos)
	{
		
		if(qos < 0 || qos > 2) {
			qos = ConfigConst.DEFAULT_QOS;
		}
		if(isConnected()) {
			_Logger.info("Subscribing to topic:" + topicName.getResourceName());
			try {
				this.mqttClient.subscribe(topicName.getResourceName(), qos);
				return true;
			} catch (MqttException e) {
				_Logger.info("Exception in Subscribing to topic:" + topicName.getResourceName());
			}
		}else {
			_Logger.warning("No connection to broker. Ignoring publish. Broker/topic: " + this.mqttClient.getCurrentServerURI() + topicName.getResourceName());
		}
		return false;
	}

	@Override
	public boolean unsubscribeFromTopic(ResourceNameEnum topicName)
	{
		if(isConnected()) {
			_Logger.info("Unsubscribing to topic:" + topicName.getResourceName());
			try {
				this.mqttClient.unsubscribe(topicName.getResourceName());
				return true;
			} catch (MqttException e) {
				_Logger.info("Exception in Unubscribing to topic:" + topicName.getResourceName());
			}
		}else {
			_Logger.warning("No connection to broker. Ignoring publish. Broker/topic: " + this.mqttClient.getCurrentServerURI() + topicName.getResourceName());
		}
		return false;
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		if(listener == null) {
			return false;
		}
		this.dataMsgListener = listener;
		return true;
	}
	
	// callbacks
	
	@Override
	public void connectComplete(boolean reconnect, String serverURI)
	{
		_Logger.info("[callback]MqttClientConn%connectComplete reconnect[" + reconnect + "], URI[" + serverURI + "]");
	}

	@Override
	public void connectionLost(Throwable t)
	{
		_Logger.info("[callback]MqttClientConn%connectionLost");
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		_Logger.info("[callback]MqttClientConn%deliveryComplete");
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception
	{
		_Logger.info("[callback]MqttClientConn%messageArrived topic[" + topic + "], message[" + msg + "]");
	}

	
	// private methods
	
	/**
	 * Called by the constructor to set the MQTT client parameters to be used for the connection.
	 * 
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initClientParameters(String configSectionName)
	{
		// TODO: implement this
	}
	
	/**
	 * Called by {@link #initClientParameters(String)} to load credentials.
	 * 
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initCredentialConnectionParameters(String configSectionName)
	{
		// TODO: implement this
	}
	
	/**
	 * Called by {@link #initClientParameters(String)} to enable encryption.
	 * 
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initSecureConnectionParameters(String configSectionName)
	{
		// TODO: implement this
	}
}
