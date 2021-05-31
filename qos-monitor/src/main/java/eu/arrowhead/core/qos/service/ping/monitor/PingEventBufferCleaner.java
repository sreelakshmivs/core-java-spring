package eu.arrowhead.core.qos.service.ping.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.arrowhead.core.qos.QosMonitorConstants;

@Service
public class PingEventBufferCleaner {
	//=================================================================================================
	// members

	private final long MAX_TIME_BEFORE_CLEAR = 1000 * 60 * 10 * 2;

	@Resource(name = QosMonitorConstants.EVENT_BUFFER)
	private ConcurrentHashMap<UUID, PingEventBufferElement> eventBuffer;

	private final Logger logger = LogManager.getLogger(PingEventBufferCleaner.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public void clearBuffer() {
		logger.debug("clearBuffer started");

		final Collection<PingEventBufferElement> entries = eventBuffer.values();

		for (final Iterator<PingEventBufferElement> iterator = entries.iterator(); iterator.hasNext();) {
			final PingEventBufferElement element = (PingEventBufferElement) iterator.next();
			if (element.getCreatedAt() < (System.currentTimeMillis() - MAX_TIME_BEFORE_CLEAR)) {

				logger.debug("Clearing element from buffer: " + element.getId());
				eventBuffer.remove(element.getId());
			}

		}
	}
}
