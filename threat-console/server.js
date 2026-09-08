import express from 'express';
import cors from 'cors';
import http from 'http';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const server = http.createServer(app);
const PORT = process.env.PORT || 8085;

app.use(cors());
app.use(express.json());

// In-memory store for received threat events and connection state
let threatEvents = [];
const processedEventIds = new Set();
let clients = [];
let lastPhoneHeartbeat = 0;
const HEARTBEAT_TIMEOUT_MS = 15000;

// Helper to broadcast event to connected web clients (Server-Sent Events)
function broadcast(data) {
  clients.forEach((client) => {
    client.res.write(`data: ${JSON.stringify(data)}\n\n`);
  });
}

// SSE endpoint for live React console updates
app.get('/api/events-stream', (req, res) => {
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.flushHeaders();

  const clientId = Date.now();
  const newClient = { id: clientId, res };
  clients.push(newClient);

  // Send initial connection handshake and event history
  res.write(`data: ${JSON.stringify({ type: 'INIT', events: threatEvents, phoneConnected: isPhoneConnected() })}\n\n`);

  req.on('close', () => {
    clients = clients.filter((c) => c.id !== clientId);
  });
});

function isPhoneConnected() {
  return Date.now() - lastPhoneHeartbeat < HEARTBEAT_TIMEOUT_MS;
}

// PING / PONG Endpoint for connection diagnostics
app.post('/api/ping', (req, res) => {
  const clientIp = req.ip || req.socket.remoteAddress || 'Unknown';
  lastPhoneHeartbeat = Date.now();

  console.log(`[SCAMSHIELD SERVER] Client connected: ${clientIp} | Received: PING | Sent: PONG`);

  broadcast({ type: 'STATUS_UPDATE', phoneConnected: true, lastClientIp: clientIp });

  return res.status(200).json({
    type: 'PONG',
    status: 'CONNECTED',
    device: req.body ? req.body.device || 'Android Device' : 'Android Device',
    clientIp: clientIp,
    laptopHost: '0.0.0.0',
    port: PORT,
    timestamp: Date.now()
  });
});

// POST endpoint consumed by ScamShield X Android app
app.post('/api/threat-event', (req, res) => {
  const event = req.body;

  if (!event || !event.eventId) {
    return res.status(400).json({ error: 'Invalid threat event payload' });
  }

  // Deduplication check using eventId
  if (processedEventIds.has(event.eventId)) {
    return res.status(200).json({ status: 'DUPLICATE_IGNORED', eventId: event.eventId });
  }

  processedEventIds.add(event.eventId);
  lastPhoneHeartbeat = Date.now();

  const formattedEvent = {
    eventId: event.eventId,
    timestamp: event.timestamp || Date.now(),
    source: event.source || 'Notification Shield',
    sourceApp: event.sourceApp || null,
    messageType: event.messageType || 'GENERAL',
    riskScore: typeof event.riskScore === 'number' ? event.riskScore : 0,
    riskLevel: event.riskLevel || 'LOW',
    confidencePercent: typeof event.confidencePercent === 'number' ? event.confidencePercent : 85,
    signals: Array.isArray(event.signals) ? event.signals : [],
    threatChain: Array.isArray(event.threatChain) ? event.threatChain : ['MESSAGE', 'LOW RISK'],
    domain: event.domain || null,
    recommendedAction: event.recommendedAction || 'No immediate action required.',
    isDevTest: event.isDevTest || false
  };

  // Prepend new event to timeline (limit to 100 recent events)
  threatEvents.unshift(formattedEvent);
  if (threatEvents.length > 100) {
    threatEvents = threatEvents.slice(0, 100);
  }

  console.log(`[SCAMSHIELD CONSOLE] Threat event received: ${formattedEvent.messageType} (${formattedEvent.riskScore}/100) from ${formattedEvent.source}`);

  // Broadcast to live web dashboard
  broadcast({
    type: 'NEW_THREAT_EVENT',
    event: formattedEvent,
    phoneConnected: true
  });

  return res.status(200).json({ status: 'EVENT_RECEIVED', eventId: event.eventId });
});

// Phone Heartbeat endpoint
app.post('/api/heartbeat', (req, res) => {
  lastPhoneHeartbeat = Date.now();
  broadcast({ type: 'STATUS_UPDATE', phoneConnected: true });
  res.status(200).json({ status: 'HEARTBEAT_ACK' });
});

// Status Endpoint
app.get('/api/status', (req, res) => {
  res.json({
    status: 'ONLINE',
    phoneConnected: isPhoneConnected(),
    totalEventsCount: threatEvents.length,
    lastHeartbeatTime: lastPhoneHeartbeat
  });
});

// Get stored events list
app.get('/api/threat-events', (req, res) => {
  res.json({
    events: threatEvents,
    phoneConnected: isPhoneConnected()
  });
});

// Clear events endpoint
app.post('/api/clear-events', (req, res) => {
  threatEvents = [];
  processedEventIds.clear();
  broadcast({ type: 'CLEAR_EVENTS', events: [] });
  res.json({ status: 'CLEARED' });
});

// Serve static frontend build if present
app.use(express.static(path.join(__dirname, 'dist')));
app.get('*', (req, res) => {
  if (req.path.startsWith('/api')) return;
  const indexPath = path.join(__dirname, 'dist', 'index.html');
  res.sendFile(indexPath, (err) => {
    if (err) {
      res.send('ScamShield X Threat Console Backend active. Run `npm run dev` to launch the React frontend.');
    }
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(`SCAMSHIELD X THREAT CONSOLE SERVER ACTIVE`);
  console.log(`Console Listening on: 0.0.0.0:${PORT}`);
  console.log(`Android Local IP Endpoint: http://10.0.2.2:${PORT}/api/threat-event`);
  console.log(`=======================================================`);
});
