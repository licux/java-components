/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.system;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.app.GatewayDeviceApp;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemPerformanceManager
{
	// private var's
	private static final Logger _Logger =	Logger.getLogger(SystemPerformanceManager.class.getName());
	
	private int pollRate = ConfigConst.DEFAULT_POLL_CYCLES;
	
	private ScheduledExecutorService schedExecSvc = null;
	private SystemCpuUtilTask cpuUtilTask = null;
	private SystemMemUtilTask memUtilTask = null;
	
	private String locationID = ConfigConst.GATEWAY_DEVICE; 
	private Runnable taskRunner = null;
	private boolean isStarted = false;
	
	
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public SystemPerformanceManager()
	{
		_Logger.info("SystemPerformanceManager is initializing..."); 
		this.pollRate = ConfigUtil.getInstance().getInteger(ConfigConst.GATEWAY_DEVICE, ConfigConst.POLL_CYCLES_KEY, ConfigConst.DEFAULT_POLL_CYCLES);
		if(this.pollRate <= 0) {
			this.pollRate = ConfigConst.DEFAULT_POLL_CYCLES;
		}
		this.locationID = ConfigUtil.getInstance().getProperty(ConfigConst.GATEWAY_DEVICE, ConfigConst.DEVICE_LOCATION_ID_KEY, ConfigConst.GATEWAY_DEVICE); 
		
		this.schedExecSvc = Executors.newScheduledThreadPool(1);
		this.cpuUtilTask = new SystemCpuUtilTask();
		this.memUtilTask = new SystemMemUtilTask();
		
		this.taskRunner = () -> {
			this.handleTelemetry();
		};
	}
	
	
	// public methods
	
	public void handleTelemetry()
	{
		float cpuUtilPct = this.cpuUtilTask.getTelemetryValue();
		float memUtilPct = this.memUtilTask.getTelemetryValue();
		_Logger.info("Handle telemetry results: cpuTil = " + cpuUtilPct + ", memUtil = " + memUtilPct);
	}
	
	public void setDataMessageListener(IDataMessageListener listener)
	{
	}
	
	public void startManager()
	{
		_Logger.info("SystemPerformanceManager is starting...");  
		if(!this.isStarted) {
			ScheduledFuture<?> futrueTask = this.schedExecSvc.scheduleAtFixedRate(this.taskRunner, 0L, this.pollRate, TimeUnit.SECONDS);
			this.isStarted = true;
		}
	}
	
	public void stopManager()
	{
		_Logger.info("SystemPerformanceManager is stopped.");
		this.schedExecSvc.shutdown();
	}
	
}
