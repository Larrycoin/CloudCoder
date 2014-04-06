// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.healthmonitor;

import org.cloudcoder.healthmonitor.HealthMonitorReport.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Health monitor agent.  Periodically contacts CloudCoder webapp
 * instances to ensure that they are responsive and that they
 * have builders connected to them.  Sends email if an instance
 * is found to be in an unhealthy state.
 * 
 * @author David Hovemeyer
 */
public class HealthMonitor implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(HealthMonitorConfig.class);
	
	// Check instances every 5 minutes
	private static final int CHECK_INTERVAL_SEC = 5*60;
	
	private Object lock;
	private HealthMonitorConfig config;
	private volatile boolean shutdown;

	public HealthMonitor() {
		lock = new Object();
	}
	
	/**
	 * Set the {@link HealthMonitorConfig} that specifies the webapp instances to
	 * monitor and the email address where problems should be reported.
	 * 
	 * @param config
	 */
	public void setConfig(HealthMonitorConfig config) {
		synchronized (lock) {
			this.config = config;
		}
	}
	
	/**
	 * Shut down the health monitor.  Note that the thread executing
	 * the health monitor should be interrupted to ensure timely shutdown.
	 * (Do that after calling this method.
	 */
	public void shutdown() {
		shutdown = true;
		// Caller is responsible for interrupting the thread
	}
	
	@Override
	public void run() {
		while (!shutdown) {
			try {
				Thread.sleep(CHECK_INTERVAL_SEC);
				
				// Make a copy of the config (the "real" config can be set
				// asynchronously)
				HealthMonitorConfig config;
				synchronized (lock) {
					config = this.config.clone();
				}
				
				// Check instances
				HealthMonitorReport report = new HealthMonitorReport();
				for (String instance : config.getWebappInstanceList()) {
					report.addEntry(checkInstance(instance));
				}
			} catch (InterruptedException e) {
				logger.info("HealthMonitor interrupted (shutdown requested?)");
			}
		}
	}

	private Entry checkInstance(String instance) {
		// FIXME: implement this
		return new HealthMonitorReport.Entry(instance, HealthMonitorReport.Status.HEALTHY);
	}

}