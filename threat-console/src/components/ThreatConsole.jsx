import React, { useState, useEffect } from 'react';

// Cyber Terminal Activity Feed Log Component
function CyberTerminalLog({ events }) {
  const [logs, setLogs] = useState([
    "[SYSTEM] ScamShield X Receiver listening on 0.0.0.0:8085...",
    "[OFFICE_KIT] SSE live event stream established.",
    "[SECURITY_SENSOR] Notification Shield active."
  ]);

  useEffect(() => {
    if (events && events.length > 0) {
      const latest = events[0];
      const timeStr = new Date(latest.timestamp).toLocaleTimeString();
      const newLog = `[${timeStr}] EVENT_RECV: ${latest.messageType} (${latest.riskScore}/100) -> ${latest.riskLevel}`;
      setLogs((prev) => [newLog, ...prev.slice(0, 5)]);
    }
  }, [events]);

  return (
    <div className="cyber-terminal-box hud-card">
      <div className="corner-tl"></div><div className="corner-tr"></div>
      <div className="corner-bl"></div><div className="corner-br"></div>
      <div className="terminal-header mono">
        <span>LIVE SENSOR TERMINAL</span>
        <span className="text-cyan">HTTP 200 OK</span>
      </div>
      <div className="terminal-body mono">
        {logs.map((lg, i) => (
          <div key={i} className="terminal-line">
            <span className="text-cyan">&gt;</span> {lg}
          </div>
        ))}
      </div>
    </div>
  );
}

export default function ThreatConsole({ onBackToLanding }) {
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [phoneConnected, setPhoneConnected] = useState(false);
  const [filter, setFilter] = useState('ALL');
  const [serverHost, setServerHost] = useState(window.location.hostname || 'localhost');

  useEffect(() => {
    const eventSource = new EventSource('/api/events-stream');

    eventSource.onmessage = (e) => {
      try {
        const data = JSON.parse(e.data);
        if (data.type === 'INIT') {
          setEvents(data.events || []);
          if (data.events && data.events.length > 0) {
            setSelectedEvent(data.events[0]);
          }
          setPhoneConnected(!!data.phoneConnected);
        } else if (data.type === 'NEW_THREAT_EVENT') {
          setEvents((prev) => [data.event, ...prev.filter((ev) => ev.eventId !== data.event.eventId)]);
          setSelectedEvent(data.event);
          setPhoneConnected(true);
        } else if (data.type === 'STATUS_UPDATE') {
          setPhoneConnected(!!data.phoneConnected);
        } else if (data.type === 'CLEAR_EVENTS') {
          setEvents([]);
          setSelectedEvent(null);
        }
      } catch (err) {
        console.error('SSE Message error:', err);
      }
    };

    eventSource.onerror = () => {
      setPhoneConnected(false);
    };

    return () => {
      eventSource.close();
    };
  }, []);

  useEffect(() => {
    const interval = setInterval(() => {
      fetch('/api/status')
        .then((res) => res.json())
        .then((data) => {
          setPhoneConnected(!!data.phoneConnected);
        })
        .catch(() => setPhoneConnected(false));
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  const totalCount = events.length;
  const highCount = events.filter((e) => e.riskLevel === 'HIGH_RISK').length;
  const suspiciousCount = events.filter((e) => e.riskLevel === 'SUSPICIOUS').length;
  const lowCount = events.filter((e) => e.riskLevel === 'LOW').length;

  const filteredEvents = events.filter((ev) => {
    if (filter === 'HIGH') return ev.riskLevel === 'HIGH_RISK';
    if (filter === 'MEDIUM') return ev.riskLevel === 'SUSPICIOUS';
    if (filter === 'LOW') return ev.riskLevel === 'LOW';
    return true;
  });

  const triggerDevTestEvent = (isHighRisk = true) => {
    const devEvent = {
      eventId: 'DEV_TEST_' + Date.now(),
      timestamp: Date.now(),
      source: 'Notification Shield',
      sourceApp: 'com.whatsapp',
      messageType: isHighRisk ? 'OTP / ACCOUNT SECURITY' : 'INTERNSHIP',
      riskScore: isHighRisk ? 92 : 12,
      riskLevel: isHighRisk ? 'HIGH_RISK' : 'LOW',
      confidencePercent: 88,
      signals: isHighRisk
        ? ['OTP REQUEST', 'URGENT ACTION', 'LOOK-ALIKE DOMAIN', 'ACCOUNT VERIFICATION']
        : ['APPLICATION NOTICE'],
      threatChain: isHighRisk
        ? ['MESSAGE', 'OTP REQUEST', 'URGENT VERIFICATION', 'SUSPICIOUS LINK', 'LOOK-ALIKE DOMAIN', 'CRITICAL']
        : ['MESSAGE', 'INTERNSHIP', 'LOW RISK'],
      domain: isHighRisk ? 'github.com.verification.invalid' : 'official-portal.org',
      recommendedAction: isHighRisk
        ? "DON'T CLICK • DON'T PAY • DON'T SHARE OTP"
        : 'Safe to proceed. Verify context if sharing details.',
      isDevTest: true
    };

    fetch('/api/threat-event', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(devEvent)
    });
  };

  const clearEvents = () => {
    fetch('/api/clear-events', { method: 'POST' });
  };

  return (
    <div className="console-container">
      {/* Top Header Bar */}
      <header className="top-header">
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            {onBackToLanding && (
              <button className="btn-outlined" onClick={onBackToLanding} style={{ fontSize: '10px' }}>
                ← BACK TO PRODUCT LANDING
              </button>
            )}
            <div className="brand-title">
              <span className="brand-icon">🛡</span>
              SCAMSHIELD X
              <span className="brand-badge">THREAT CONSOLE v1.0</span>
            </div>
          </div>
          <div className="mono" style={{ fontSize: '11px', color: '#737373', marginTop: '6px' }}>
            OFFICE KIT COMMAND CENTER • LIVE SENSOR STREAM
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div className="status-badge">
            <span className={`dot-indicator ${phoneConnected ? 'dot-active' : 'dot-offline'}`}></span>
            <span style={{ color: phoneConnected ? '#00E676' : '#737373' }}>
              {phoneConnected ? 'PHONE CONNECTED' : 'PHONE OFFLINE'}
            </span>
          </div>

          <button className="btn-outlined btn-test" onClick={() => triggerDevTestEvent(true)}>
            + DEV HIGH TEST
          </button>
          <button className="btn-outlined btn-test" onClick={() => triggerDevTestEvent(false)}>
            + DEV LEGIT TEST
          </button>
        </div>
      </header>

      {/* Cyber Terminal Tactical Row */}
      <div className="tactical-hud-row">
        <CyberTerminalLog events={events} />
      </div>

      {/* Summary Metrics Row */}
      <div className="metrics-row">
        <div className="metric-card hud-card">
          <div className="corner-tl"></div><div className="corner-tr"></div>
          <div className="corner-bl"></div><div className="corner-br"></div>
          <div className="metric-label">TOTAL THREATS</div>
          <div className="metric-value" style={{ color: '#ffffff' }}>
            {totalCount}
          </div>
        </div>

        <div className="metric-card hud-card">
          <div className="corner-tl"></div><div className="corner-tr"></div>
          <div className="corner-bl"></div><div className="corner-br"></div>
          <div className="metric-label">HIGH RISK</div>
          <div className="metric-value" style={{ color: '#FF2A2A' }}>
            {highCount}
          </div>
        </div>

        <div className="metric-card hud-card">
          <div className="corner-tl"></div><div className="corner-tr"></div>
          <div className="corner-bl"></div><div className="corner-br"></div>
          <div className="metric-label">MEDIUM / SUSPICIOUS</div>
          <div className="metric-value" style={{ color: '#FFB300' }}>
            {suspiciousCount}
          </div>
        </div>

        <div className="metric-card hud-card">
          <div className="corner-tl"></div><div className="corner-tr"></div>
          <div className="corner-bl"></div><div className="corner-br"></div>
          <div className="metric-label">LOW RISK</div>
          <div className="metric-value" style={{ color: '#00E676' }}>
            {lowCount}
          </div>
        </div>
      </div>

      {/* Main Grid: Live Threat Panel & Sidebar Timeline */}
      <div className="dashboard-grid">
        {/* Left Column: Live Threat Investigation Panel */}
        <div>
          {selectedEvent ? (
            <div
              className={`threat-card hud-card ${
                selectedEvent.riskLevel === 'HIGH_RISK'
                  ? 'critical'
                  : selectedEvent.riskLevel === 'SUSPICIOUS'
                  ? 'suspicious'
                  : 'low'
              }`}
            >
              <div className="corner-tl"></div><div className="corner-tr"></div>
              <div className="corner-bl"></div><div className="corner-br"></div>

              <div className="card-header-label">
                <span>01 LIVE THREAT INVESTIGATION</span>
                <span>ID: {selectedEvent.eventId.substring(0, 12)}</span>
              </div>

              <div
                className={`threat-headline ${
                  selectedEvent.riskLevel === 'HIGH_RISK'
                    ? 'critical'
                    : selectedEvent.riskLevel === 'SUSPICIOUS'
                    ? 'suspicious'
                    : 'low'
                }`}
              >
                {selectedEvent.riskLevel === 'HIGH_RISK'
                  ? '🚨 POTENTIAL FRAUD DETECTED'
                  : selectedEvent.riskLevel === 'SUSPICIOUS'
                  ? '⚠️ SUSPICIOUS MESSAGE'
                  : '✓ LOW RISK'}
              </div>

              <div className="score-meta-grid">
                <div className="score-box">
                  <div
                    className="score-num"
                    style={{
                      color:
                        selectedEvent.riskLevel === 'HIGH_RISK'
                          ? '#FF2A2A'
                          : selectedEvent.riskLevel === 'SUSPICIOUS'
                          ? '#FFB300'
                          : '#00E676'
                    }}
                  >
                    {selectedEvent.riskScore}
                  </div>
                  <div className="score-denom">/ 100 RISK SCORE</div>
                </div>

                <div className="meta-details">
                  <div className="meta-row">
                    <span className="meta-key">MESSAGE TYPE</span>
                    <span className="meta-val">{selectedEvent.messageType}</span>
                  </div>
                  <div className="meta-row">
                    <span className="meta-key">SOURCE SENSOR</span>
                    <span className="meta-val">{selectedEvent.source}</span>
                  </div>
                  {selectedEvent.sourceApp && (
                    <div className="meta-row">
                      <span className="meta-key">SOURCE APP</span>
                      <span className="meta-val">{selectedEvent.sourceApp}</span>
                    </div>
                  )}
                  {selectedEvent.domain && (
                    <div className="meta-row">
                      <span className="meta-key">TARGET DOMAIN</span>
                      <span className="meta-val">{selectedEvent.domain}</span>
                    </div>
                  )}
                  <div className="meta-row">
                    <span className="meta-key">CONFIDENCE</span>
                    <span className="meta-val">{selectedEvent.confidencePercent}%</span>
                  </div>
                </div>
              </div>

              {/* Threat Chain */}
              <div className="threat-chain-container">
                <div className="mono" style={{ fontSize: '11px', color: '#00E5FF', fontWeight: 'bold' }}>
                  02 VISUAL THREAT CHAIN
                </div>

                <div className="chain-flow">
                  {selectedEvent.threatChain && selectedEvent.threatChain.length > 0 ? (
                    selectedEvent.threatChain.map((node, idx) => {
                      const isLast = idx === selectedEvent.threatChain.length - 1;
                      return (
                        <React.Fragment key={idx}>
                          <div className={`chain-node ${isLast ? 'terminal' : ''}`}>
                            {node}
                          </div>
                          {!isLast && <div className="chain-arrow">↓</div>}
                        </React.Fragment>
                      );
                    })
                  ) : (
                    <div className="chain-node">MESSAGE → {selectedEvent.messageType} → {selectedEvent.riskLevel}</div>
                  )}
                </div>
              </div>

              {/* Detected Signals */}
              <div style={{ marginTop: '20px' }}>
                <div className="mono" style={{ fontSize: '11px', color: '#737373', fontWeight: 'bold' }}>
                  03 DETECTED SIGNALS ({selectedEvent.signals.length})
                </div>

                {selectedEvent.signals.length === 0 ? (
                  <div className="mono" style={{ fontSize: '11px', color: '#737373', marginTop: '8px' }}>
                    No significant suspicious signals recorded for this event.
                  </div>
                ) : (
                  <div className="signals-grid">
                    {selectedEvent.signals.map((sig, idx) => (
                      <div className="signal-item" key={idx}>
                        ⚠ {sig}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Action Bar */}
              <div style={{ marginTop: '24px', display: 'flex', gap: '12px' }}>
                <button className="btn-action">
                  {selectedEvent.riskLevel === 'HIGH_RISK' ? "DON'T PROCEED" : 'VIEW DETAILS'}
                </button>
                <button className="btn-outlined">OPEN ON PHONE</button>
              </div>
            </div>
          ) : (
            <div className="empty-state hud-card">
              <div className="corner-tl"></div><div className="corner-tr"></div>
              <div className="corner-bl"></div><div className="corner-br"></div>

              <div className="empty-title">
                {phoneConnected ? '● PHONE CONNECTED' : '○ WAITING FOR PHONE'}
              </div>
              <div className="empty-sub">
                {phoneConnected
                  ? 'No active threats recorded yet. Waiting for mobile security events...'
                  : 'Ensure ScamShield X Android app is running and connected on local network.'}
              </div>
              <div style={{ marginTop: '20px' }}>
                <div className="mono" style={{ fontSize: '11px', color: '#00E5FF' }}>
                  Local Receiver Endpoint: http://{serverHost}:8085/api/threat-event
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Timeline Panel */}
        <div className="timeline-panel hud-card">
          <div className="corner-tl"></div><div className="corner-tr"></div>
          <div className="corner-bl"></div><div className="corner-br"></div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div className="mono" style={{ fontSize: '12px', fontWeight: 'bold', color: '#00E5FF' }}>
              THREAT EVENTS ({filteredEvents.length})
            </div>
            {events.length > 0 && (
              <button
                style={{ background: 'none', border: 'none', color: '#737373', fontSize: '10px', cursor: 'pointer' }}
                onClick={clearEvents}
              >
                CLEAR
              </button>
            )}
          </div>

          {/* Filter Bar */}
          <div style={{ display: 'flex', gap: '6px', marginTop: '12px' }}>
            {['ALL', 'HIGH', 'MEDIUM', 'LOW'].map((f) => (
              <button
                key={f}
                className="btn-outlined"
                style={{
                  padding: '4px 8px',
                  fontSize: '10px',
                  borderColor: filter === f ? '#00E5FF' : '#262626',
                  color: filter === f ? '#00E5FF' : '#737373'
                }}
                onClick={() => setFilter(f)}
              >
                {f}
              </button>
            ))}
          </div>

          <div className="timeline-list">
            {filteredEvents.length === 0 ? (
              <div className="mono" style={{ fontSize: '11px', color: '#737373', padding: '20px 0', textAlign: 'center' }}>
                No events in timeline.
              </div>
            ) : (
              filteredEvents.map((ev) => {
                const isSelected = selectedEvent && selectedEvent.eventId === ev.eventId;
                const timeStr = new Date(ev.timestamp).toLocaleTimeString();
                const levelColor =
                  ev.riskLevel === 'HIGH_RISK' ? '#FF2A2A' : ev.riskLevel === 'SUSPICIOUS' ? '#FFB300' : '#00E676';

                return (
                  <div
                    key={ev.eventId}
                    className={`timeline-item ${isSelected ? 'active' : ''}`}
                    onClick={() => setSelectedEvent(ev)}
                  >
                    <div className="timeline-top">
                      <span style={{ color: levelColor }}>
                        {ev.riskLevel === 'HIGH_RISK' ? 'CRITICAL' : ev.riskLevel === 'SUSPICIOUS' ? 'SUSPICIOUS' : 'LOW RISK'}
                      </span>
                      <span>{timeStr}</span>
                    </div>

                    <div className="timeline-cat">{ev.messageType}</div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '6px' }}>
                      <span className="mono" style={{ fontSize: '10px', color: '#737373' }}>
                        {ev.source}
                      </span>
                      <span className="mono" style={{ fontSize: '12px', fontWeight: 'bold', color: levelColor }}>
                        {ev.riskScore} / 100
                      </span>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
