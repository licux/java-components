/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.data;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;

/**
 * Shell representation of class for student implementation.
 *
 */
public class DataUtil
{
	// static
	
	private static final DataUtil _Instance = new DataUtil();

	/**
	 * Returns the Singleton instance of this class.
	 * 
	 * @return ConfigUtil
	 */
	public static final DataUtil getInstance()
	{
		return _Instance;
	}
	
	
	// private var's
	
	
	// constructors
	
	/**
	 * Default (private).
	 * 
	 */
	private DataUtil()
	{
		super();
	}
	
	
	// public methods
	
	public String actuatorDataToJson(ActuatorData actuatorData)
	{
		String jsonData = null;
		if(actuatorData != null) {
			Gson converter = new Gson();
			jsonData = converter.toJson(actuatorData);
		}
		return jsonData;
	}
	
	public String sensorDataToJson(SensorData sensorData)
	{
		String jsonData = null;
		if(sensorData != null) {
			Gson converter = new Gson();
			jsonData = converter.toJson(sensorData);
		}
		return jsonData;
	}
	
	public String systemPerformanceDataToJson(SystemPerformanceData sysPerfData)
	{
		String jsonData = null;
		if(sysPerfData != null) {
			Gson converter = new Gson();
			jsonData = converter.toJson(sysPerfData);
		}
		return jsonData;
	}
	
	public String systemStateDataToJson(SystemStateData sysStateData)
	{
		String jsonData = null;
		if(sysStateData != null) {
			Gson converter = new Gson();
			jsonData = converter.toJson(sysStateData);
		}
		return jsonData;
	}
	
	public ActuatorData jsonToActuatorData(String jsonData)
	{
		ActuatorData actuatorData = null;
		if(jsonData != null && jsonData.trim().length() > 0) {
			Gson converter = new Gson();
			actuatorData = converter.fromJson(jsonData, ActuatorData.class);
		}
		return actuatorData;
	}
	
	public SensorData jsonToSensorData(String jsonData)
	{
		SensorData sensorData = null;
		if(jsonData != null && jsonData.trim().length() > 0) {
			Gson converter = new Gson();
			sensorData = converter.fromJson(jsonData, SensorData.class);
		}
		return sensorData;
	}
	
	public SystemPerformanceData jsonToSystemPerformanceData(String jsonData)
	{
		SystemPerformanceData sysPerfData = null;
		if(jsonData != null && jsonData.trim().length() > 0) {
			Gson converter = new Gson();
			sysPerfData = converter.fromJson(jsonData, SystemPerformanceData.class);
		}
		return sysPerfData;
	}
	
	public SystemStateData jsonToSystemStateData(String jsonData)
	{
		SystemStateData systemStateData = null;
		if(jsonData != null && jsonData.trim().length() > 0) {
			Gson converter = new Gson();
			systemStateData = converter.fromJson(jsonData, SystemStateData.class);
		}
		return systemStateData;
	}
	
}
