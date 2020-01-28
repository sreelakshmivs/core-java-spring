package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "qos_intra_ping_measurement")
public class QoSIntraPingMeasurement {

	//=================================================================================================
	// members

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "measurementId", referencedColumnName = "id", nullable = false, unique = true)
	private QoSIntraMeasurement measurement;

	@Column(nullable = false)
	private boolean available = false;

	@Column(nullable = true)
	private ZonedDateTime lastAccessAt;

	@Column(nullable = true)
	private Integer minResponseTime;

	@Column(nullable = true)
	private Integer maxResponseTime;

	@Column(nullable = true)
	private Integer meanResponseTime;
	
	@Column(nullable = false)
	private long sent;

	@Column(nullable = false)
	private long received;
	
	@Column(nullable = true)
	private ZonedDateTime countStartedAt;

	@Column(nullable = false)
	private long sentAll;

	@Column(nullable = false)
	private long receivedAll;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSIntraPingMeasurement() {}

	//-------------------------------------------------------------------------------------------------
	@PrePersist
	public void onCreate() {
		this.createdAt = ZonedDateTime.now();
		this.updatedAt = this.createdAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = ZonedDateTime.now();
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public QoSIntraMeasurement getMeasurement() { return measurement; }
	public boolean isAvailable() { return available; }
	public ZonedDateTime getLastAccessAt() { return lastAccessAt; }
	public Integer getMinResponseTime() { return minResponseTime; }
	public Integer getMaxResponseTime() { return maxResponseTime; }
	public Integer getMeanResponseTime() { return meanResponseTime; }
	public long getSent() { return sent; }
	public long getReceived() { return received; }
	public ZonedDateTime getCountStartedAt() { return countStartedAt; }
	public long getSentAll() { return sentAll; }
	public long getReceivedAll() { return receivedAll; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setMeasurement(final QoSIntraMeasurement measurement) { this.measurement = measurement; }
	public void setAccessible(final boolean available) { this.available = available; }
	public void setLastAccessAt(final ZonedDateTime lastAccessAt) { this.lastAccessAt = lastAccessAt; }
	public void setMinResponseTime(final Integer minResponseTime) { this.minResponseTime = minResponseTime; }
	public void setMaxResponseTime(final Integer maxResponseTime) { this.maxResponseTime = maxResponseTime; }
	public void setMeanResponseTime(final Integer meanResponseTime) { this.meanResponseTime = meanResponseTime; }
	public void setSent(final long sent) { this.sent = sent; }
	public void setReceived(final long received) { this.received = received; }
	public void setCountStartedAt(final ZonedDateTime countStartedAt) { this.countStartedAt = countStartedAt; }
	public void setSentAll(final long sentAll) { this.sentAll = sentAll; }
	public void setReceivedAll(final long receivedAll) { this.receivedAll = receivedAll; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) {	this.updatedAt = updatedAt; }
}