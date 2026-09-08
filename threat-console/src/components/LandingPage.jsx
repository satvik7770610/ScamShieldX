import React, { useState, useEffect } from 'react';

// Configurable constants for APK & GitHub repository links
export const APK_DOWNLOAD_URL = "https://github.com/scamshield/scamshield-x/releases/latest/download/scamshield-x.apk";
export const GITHUB_REPO_URL = "https://github.com/scamshield/scamshield-x";

// Image configuration mapping if actual screenshot files are provided in public/screenshots/
export const SCREENSHOT_OVERRIDE_URLS = {
  home: "/screenshots/home.png",
  qr: "/screenshots/qr_scanner.png",
  link: "/screenshots/link_analyzer.png",
  text: "/screenshots/text_analyzer.png",
  notif: "/screenshots/notification_shield.png",
  history: "/screenshots/threat_history.png",
  settings: "/screenshots/settings_ml_lab.png"
};

export default function LandingPage({ onOpenConsole }) {
  const [activeAppScreen, setActiveAppScreen] = useState('home');
  const [animStep, setAnimStep] = useState(0);

  // Auto-advance interactive notification threat animation
  useEffect(() => {
    const timer = setInterval(() => {
      setAnimStep((prev) => (prev + 1) % 4);
    }, 3500);
    return () => clearInterval(timer);
  }, []);

  const appScreens = [
    {
      id: 'home',
      num: '01',
      title: 'HOME / DASHBOARD',
      label: 'Pre-Action Security Console',
      desc: 'Dominant hero statement, proactive Notification Shield status, primary tool shortcuts, and real threat history.'
    },
    {
      id: 'qr',
      num: '02',
      title: 'QR SCANNER',
      label: 'Payment & Web QR Security',
      desc: 'Inspects payment destinations, payee VPAs, and embedded web links before completing transaction or opening.'
    },
    {
      id: 'link',
      num: '03',
      title: 'LINK ANALYSIS',
      label: 'Root Domain Intelligence',
      desc: 'Evaluates registrable domain boundaries, brand impersonation, and look-alike character substitutions.'
    },
    {
      id: 'text',
      num: '04',
      title: 'TEXT ANALYSIS',
      label: 'Urgency & Credential Detection',
      desc: 'Identifies account suspension threats, fee demands, and confidential OTP harvesting language.'
    },
    {
      id: 'notif',
      num: '05',
      title: 'NOTIFICATION SHIELD',
      label: 'Proactive Background Sensor',
      desc: 'Intercepts incoming messaging notifications on-device and warns the user before they interact.'
    },
    {
      id: 'history',
      num: '06',
      title: 'THREAT HISTORY',
      label: 'Read-Only Audit Log',
      desc: 'Preserves a local threat log with score breakdowns, risk categories, and timestamped security signals.'
    },
    {
      id: 'settings',
      num: '07',
      title: 'SETTINGS & ML LAB',
      label: 'Diagnostics & Office Kit Sync',
      desc: 'Direct tensor prediction test lab, listener status diagnostics, and live desktop console synchronization.'
    }
  ];

  return (
    <div className="landing-wrapper">
      {/* NAVIGATION BAR */}
      <nav className="navbar">
        <div className="nav-brand">
          <span className="brand-logo-icon">🛡</span>
          <span className="brand-title">SCAMSHIELD X</span>
        </div>

        <div className="nav-links">
          <a href="#product">PRODUCT</a>
          <a href="#how-it-works">HOW IT WORKS</a>
          <a href="#app-showcase">SEE THE APP</a>
          <a href="#demo">INTERACTIVE DEMO</a>
          <a href="#threats">ATTACK SURFACES</a>
          <a href="#privacy">PRIVACY</a>
        </div>

        <button className="btn-cyan-header" onClick={onOpenConsole}>
          OPEN THREAT CONSOLE →
        </button>
      </nav>

      {/* SECTION 01 — HERO / WELCOME */}
      <section className="hero-section" id="product">
        <div className="hero-grid">
          <div className="hero-left">
            <div className="tech-tag">PRE-ACTION SECURITY / v1.0</div>

            <h1 className="hero-headline">
              WELCOME TO<br />
              <span className="text-gradient">SCAMSHIELD X.</span>
            </h1>

            <div className="hero-subheadline">
              "Don't trust.<br />Verify before you act."
            </div>

            <p className="hero-copy">
              ScamShield X is an Android mobile security system that analyzes suspicious links, QR codes,
              messages, screenshots and notifications before they become costly mistakes.
            </p>

            <div className="hero-actions">
              <button className="btn-primary-cyan" onClick={onOpenConsole}>
                OPEN THREAT CONSOLE →
              </button>

              <a
                href={APK_DOWNLOAD_URL}
                target="_blank"
                rel="noreferrer"
                className="btn-secondary-dark"
              >
                GET ANDROID PROTOTYPE
              </a>
            </div>

            <div className="hero-features-list">
              <div>✓ ON-DEVICE AI</div>
              <div>✓ EXPLAINABLE RISK</div>
              <div>✓ NOTIFICATION SHIELD</div>
              <div>✓ OFFICE KIT SYNC</div>
            </div>
          </div>

          {/* HERO CENTERPIECE: REALISTIC ANDROID DEVICE MOCKUP */}
          <div className="hero-right">
            <div className="hero-device-wrapper">
              {/* Floating Technical Badges around Phone */}
              <div className="hero-badge badge-top-left">ON-DEVICE ANALYSIS ●</div>
              <div className="hero-badge badge-top-right">NOTIFICATION SHIELD ●</div>
              <div className="hero-badge badge-bottom-right">PRIVACY-FIRST</div>

              <div className="phone-container">
                <div className="phone-frame">
                  <div className="phone-notch"></div>
                  <div className="phone-screen">
                    {/* Render Home Screen UI */}
                    <div className="phone-header">
                      <div className="mono" style={{ fontSize: '11px', fontWeight: 'bold', letterSpacing: '1px' }}>
                        SCAMSHIELD X
                      </div>
                      <div className="phone-status-dot">
                        <span className="dot-active"></span>
                      </div>
                    </div>

                    <div className="phone-body">
                      <div className="phone-tag">PRE-ACTION SECURITY</div>
                      <div className="phone-title">PROTECTION<br />ACTIVE</div>

                      <div className="phone-quote">
                        "Check before you click.<br />
                        Check before you pay.<br />
                        Check before you trust."
                      </div>

                      <div className="phone-divider"></div>

                      <div className="phone-tool-row">
                        <span>01 SCAN QR</span>
                        <span>→</span>
                      </div>
                      <div className="phone-tool-row">
                        <span>02 CHECK LINK</span>
                        <span>→</span>
                      </div>
                      <div className="phone-tool-row">
                        <span>03 ANALYZE TEXT</span>
                        <span>→</span>
                      </div>

                      <div className="phone-scan-line"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="section-divider"></div>

      {/* SECTION 02 — WHAT IS SCAMSHIELD X? */}
      <section className="about-app-section">
        <div className="section-tag">MOBILE SECURITY LAYER</div>
        <h2 className="section-heading">THE SHIELD LIVES ON YOUR PHONE.</h2>

        <p className="editorial-copy">
          ScamShield X is a mobile-first security layer designed to intervene before
          a suspicious message becomes a click, a click becomes a login, or a payment becomes a loss.
        </p>

        <div className="phone-surfaces-diagram">
          <div className="surface-node">01 QR</div>
          <div className="surface-node">02 LINK</div>
          <div className="surface-hub">
            <div className="mono" style={{ color: '#00E5FF', fontWeight: 'bold', fontSize: '14px' }}>
              SCAMSHIELD X
            </div>
            <div className="mono" style={{ color: '#737373', fontSize: '10px' }}>
              ANDROID ENGINE
            </div>
          </div>
          <div className="surface-node">03 MESSAGE</div>
          <div className="surface-node">04 SCREENSHOT</div>
          <div className="surface-node">05 NOTIFICATION</div>
        </div>
      </section>

      <div className="section-divider"></div>

      {/* SECTION 03 — ACTUAL APP SHOWCASE (INTERACTIVE) */}
      <section className="app-showcase-section" id="app-showcase">
        <div className="section-tag">INTERACTIVE MOBILE PROTOTYPE</div>
        <h2 className="section-heading">SEE THE APP.</h2>
        <p className="section-sub">Protection isn't a promise. It's a workflow.</p>

        {/* Screen Tabs Selector */}
        <div className="showcase-tabs">
          {appScreens.map((scr) => (
            <button
              key={scr.id}
              className={`tab-btn ${activeAppScreen === scr.id ? 'active' : ''}`}
              onClick={() => setActiveAppScreen(scr.id)}
            >
              <span>{scr.num}</span> {scr.title}
            </button>
          ))}
        </div>

        {/* Active Screen Display Grid */}
        <div className="showcase-grid">
          {/* Active Screen Description */}
          <div className="showcase-info">
            {appScreens.map((scr) => {
              if (scr.id !== activeAppScreen) return null;
              return (
                <div key={scr.id} className="info-box">
                  <div className="info-num">{scr.num} / 07</div>
                  <h3 className="info-title">{scr.title}</h3>
                  <div className="info-label">{scr.label}</div>
                  <p className="info-desc">{scr.desc}</p>

                  <div className="info-tech-highlights">
                    <div className="tech-item">✓ Native Jetpack Compose UI</div>
                    <div className="tech-item">✓ Dark Cybersecurity Theme</div>
                    <div className="tech-item">✓ Real-time Risk Feedback</div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Mobile Phone Mockup Rendering Selected Screen */}
          <div className="showcase-phone-wrapper">
            <div className="phone-container">
              <div className="phone-frame">
                <div className="phone-notch"></div>
                <div className="phone-screen">
                  {/* Screen Content Switcher */}
                  {renderScreenUi(activeAppScreen)}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="section-divider"></div>

      {/* SECTION 04 — NOTIFICATION SHIELD DEMO */}
      <section className="demo-section" id="demo">
        <div className="demo-header">
          <div className="section-tag">HERO DEMONSTRATION</div>
          <h2 className="section-heading">THE MOMENT BEFORE THE MISTAKE.</h2>
          <p className="section-sub">ScamShield X intercepts attacks before you interact.</p>
          <div className="synthetic-badge">SYNTHETIC DEMONSTRATION</div>
        </div>

        <div className="demo-mockup-wrapper">
          <div className="demo-phone">
            <div className="demo-phone-header">
              <span>NOTIFICATION SHIELD</span>
              <span className="dot-active"></span>
            </div>

            {/* Step 0: Incoming Notification */}
            <div className={`demo-notif-banner ${animStep >= 0 ? 'visible' : ''}`}>
              <div className="notif-app">MESSAGES • NOW</div>
              <div className="notif-title">Dear Customer</div>
              <div className="notif-text">
                Your AXIS Bank account will be suspended today. Verify immediately: https://axis-secure-update.com Share OTP if requested.
              </div>
            </div>

            {/* Step 1: Scan status */}
            {animStep >= 1 && (
              <div className="demo-scan-status">
                <span>ANALYZING CONTENT & DOMAIN SIGNALS...</span>
              </div>
            )}

            {/* Step 2: Risk Result */}
            {animStep >= 2 && (
              <div className="demo-result-card">
                <div className="demo-badge-red">🚨 POTENTIAL FRAUD DETECTED</div>

                <div className="demo-score-row">
                  <div className="demo-score">92 / 100</div>
                  <div className="demo-level">CRITICAL RISK</div>
                </div>

                <div className="demo-category">
                  <span>MESSAGE TYPE:</span>
                  <strong>BANKING PHISHING</strong>
                </div>

                {/* Step 3: Threat Signals */}
                {animStep >= 3 && (
                  <div className="demo-signals-list">
                    <div>⚠ Account suspension threat</div>
                    <div>⚠ Urgent account verification</div>
                    <div>⚠ Look-alike domain (axis-secure-update.com)</div>
                    <div>⚠ OTP / Credential request</div>
                  </div>
                )}

                <div className="demo-cta-box">
                  DON'T CLICK • DON'T PAY • DON'T SHARE OTP
                </div>
              </div>
            )}
          </div>
        </div>
      </section>

      <div className="section-divider"></div>

      {/* SECTION 05 — QR / LINK / TEXT DEMOS */}
      <section className="deep-demos-section">
        <div className="section-tag">CORE TOOLKITS</div>
        <h2 className="section-heading">DEEP TOOLKIT DEMONSTRATIONS.</h2>

        <div className="deep-demo-grid">
          {/* QR */}
          <div className="deep-card">
            <div className="deep-tag">01 / QR SECURITY</div>
            <h3>CHECK BEFORE YOU PAY.</h3>
            <p>Inspects UPI payment VPAs, merchant names, and transaction notes before you authorize transfers.</p>
            <div className="deep-code-block">
              <div>UPI URI: upi://pay?pa=agent@upi&am=2000</div>
              <div>RECIPIENT: Support Agent</div>
              <div className="text-red">NOTE: Refund Processing Fee (SCAM DETECTED)</div>
            </div>
          </div>

          {/* LINK */}
          <div className="deep-card">
            <div className="deep-tag">02 / LINK INTELLIGENCE</div>
            <h3>LOOK BEYOND THE DOMAIN NAME.</h3>
            <p>Evaluates root domains and flags brand impersonation patterns on unverified hosts.</p>
            <div className="deep-code-block">
              <div>URL: https://github.com.verification.invalid/login</div>
              <div>ROOT DOMAIN: verification.invalid</div>
              <div className="text-red">SIGNAL: URL_BRAND_IMPERSONATION (HIGH RISK)</div>
            </div>
          </div>

          {/* TEXT */}
          <div className="deep-card">
            <div className="deep-tag">03 / TEXT ANALYSIS</div>
            <h3>READ THE MESSAGE BEFORE YOU BELIEVE IT.</h3>
            <p>Distinguishes legitimate recruitment messages from upfront fee demands and OTP harvesting.</p>
            <div className="deep-code-block">
              <div>LEGIT: "Your interview is confirmed for Monday." (LOW RISK)</div>
              <div className="text-red">SCAM: "Pay ₹999 registration fee to claim job." (CRITICAL)</div>
            </div>
          </div>
        </div>
      </section>

      <div className="section-divider"></div>

      {/* SECTION 06 — THREAT CHAIN */}
      <section className="threat-chain-section">
        <div className="section-tag">EXPLAINABILITY</div>
        <h2 className="section-heading">WHY WAS THIS FLAGGED?</h2>

        <p className="chain-copy">
          ScamShield X doesn't just show a number. It visualizes the exact Threat Chain behind every security finding.
        </p>

        <div className="cinematic-chain">
          <div className="c-node">MESSAGE</div>
          <div className="c-arrow">↓</div>
          <div className="c-node">BANKING CONTEXT</div>
          <div className="c-arrow">↓</div>
          <div className="c-node">URGENT ACCOUNT SUSPENSION</div>
          <div className="c-arrow">↓</div>
          <div className="c-node">SUSPICIOUS EXTERNAL LINK</div>
          <div className="c-arrow">↓</div>
          <div className="c-node">OTP REQUEST</div>
          <div className="c-arrow">↓</div>
          <div className="c-node terminal">CRITICAL RISK</div>
        </div>
      </section>

      <div className="section-divider"></div>

      {/* SECTION 07 — ON-DEVICE INTELLIGENCE */}
      <section className="intelligence-section">
        <div className="section-tag">ARCHITECTURE</div>
        <h2 className="section-heading">INTELLIGENCE ON THE DEVICE.</h2>

        <p className="intelligence-copy">
          Machine learning understands context. Deterministic rules identify concrete evidence.
          Risk fusion combines both into an explainable result.
        </p>

        <div className="tech-pipeline">
          <div className="pipe-step">ANDROID INPUT</div>
          <div className="pipe-arrow">→</div>
          <div className="pipe-step highlight">ON-DEVICE ML + DETERMINISTIC RULES</div>
          <div className="pipe-arrow">→</div>
          <div className="pipe-step">RISK FUSION</div>
          <div className="pipe-arrow">→</div>
          <div className="pipe-step">EXPLANATION</div>
          <div className="pipe-arrow">→</div>
          <div className="pipe-step alert">WARNING</div>
        </div>
      </section>

      <div className="section-divider"></div>

      {/* SECTION 08 — MULTIPLE ATTACK SURFACES */}
      <section className="surfaces-section" id="threats">
        <div className="section-tag">COVERAGE</div>
        <h2 className="section-heading">ONE SHIELD.<br />MULTIPLE ATTACK SURFACES.</h2>

        <div className="surfaces-grid">
          <div className="surface-box">
            <div className="surface-icon">📱</div>
            <h3>NOTIFICATIONS</h3>
            <p>On-device listener inspects incoming SMS, WhatsApp, and app notifications in real time.</p>
          </div>
          <div className="surface-box">
            <div className="surface-icon">🔗</div>
            <h3>LINKS & URLS</h3>
            <p>Analyzes registrable domains, brand impersonation, look-alike characters, and shorteners.</p>
          </div>
          <div className="surface-box">
            <div className="surface-icon">📷</div>
            <h3>QR CODES</h3>
            <p>Parses UPI payment URIs and embedded links before scanning or confirming payment.</p>
          </div>
          <div className="surface-box">
            <div className="surface-icon">🖼</div>
            <h3>SCREENSHOTS</h3>
            <p>Extracts text and URLs from gallery screenshots using local ML Kit OCR.</p>
          </div>
          <div className="surface-box">
            <div className="surface-icon">💬</div>
            <h3>MESSAGES & TEXT</h3>
            <p>Evaluates urgency coercion, fee demands, and credential harvesting patterns.</p>
          </div>
          <div className="surface-box">
            <div className="surface-icon">💳</div>
            <h3>PAYMENT CONTEXT</h3>
            <p>Cross-references recipient VPAs and transaction notes against known scam vectors.</p>
          </div>
        </div>
      </section>

      <div className="section-divider"></div>

      {/* SECTION 09 — PHONE → THREAT CONSOLE */}
      <section className="console-promo-section">
        <div className="section-tag">COMMAND CENTER</div>
        <h2 className="section-heading">FROM YOUR PHONE<br />TO THE THREAT CONSOLE.</h2>

        <p className="console-promo-copy">
          Threat events generated by the Android prototype can stream into the desktop Threat Console for a wider operational view.
        </p>

        <div className="flow-bridge">
          <div>[ ANDROID PHONE ]</div>
          <div>→</div>
          <div>OFFICE KIT PROTOCOL</div>
          <div>→</div>
          <div className="highlight">[ THREAT CONSOLE ]</div>
        </div>

        <div className="console-cta-row">
          <button className="btn-primary-cyan" onClick={onOpenConsole}>
            OPEN LIVE THREAT CONSOLE →
          </button>
        </div>
      </section>

      <div className="section-divider"></div>

      {/* SECTION 10 — PRIVACY */}
      <section className="privacy-section" id="privacy">
        <div className="section-tag">ZERO CLOUD TRUST</div>
        <h2 className="section-heading">YOUR PHONE.<br />YOUR SIGNALS.<br />YOUR CONTROL.</h2>

        <p className="privacy-copy">
          ScamShield X is designed around privacy-first, pre-action protection.
          Analysis happens on-device wherever possible, keeping your messages and financial details private.
        </p>
      </section>

      <div className="section-divider"></div>

      {/* SECTION 11 — TEST THE PROTOTYPE */}
      <section className="prototype-section">
        <div className="section-tag">HACKATHON PROTOTYPE</div>
        <h2 className="section-heading">READY TO TEST THE SHIELD?</h2>
        <p className="prototype-copy">Install the Android prototype and experience ScamShield X directly on a device.</p>

        <div className="prototype-btn-row">
          <a
            href={APK_DOWNLOAD_URL}
            target="_blank"
            rel="noreferrer"
            className="btn-primary-cyan"
          >
            DOWNLOAD ANDROID APK
          </a>

          <a
            href={GITHUB_REPO_URL}
            target="_blank"
            rel="noreferrer"
            className="btn-secondary-dark"
          >
            VIEW SOURCE CODE
          </a>
        </div>
      </section>

      {/* FINAL SECTION & FOOTER */}
      <section className="final-cta-section">
        <h2 className="final-headline">
          BEFORE YOU CLICK.<br />
          BEFORE YOU PAY.<br />
          BEFORE YOU TRUST.
        </h2>

        <div className="final-brand">SCAMSHIELD X</div>
        <div className="final-motto">DETECT. EXPLAIN. WARN. PROTECT.</div>

        <button className="btn-primary-cyan" onClick={onOpenConsole} style={{ marginTop: '24px' }}>
          OPEN THREAT CONSOLE →
        </button>
      </section>

      <footer className="footer">
        <div className="footer-grid">
          <div>
            <div className="footer-logo">🛡 SCAMSHIELD X</div>
            <p className="footer-sub">PRE-ACTION SECURITY</p>
          </div>

          <div className="footer-links">
            <a href="#product">Product</a>
            <a href="#how-it-works">How It Works</a>
            <a href="#app-showcase">See The App</a>
            <button onClick={onOpenConsole} className="link-button">Threat Console</button>
            <a href={APK_DOWNLOAD_URL} target="_blank" rel="noreferrer">Android Prototype</a>
            <a href={GITHUB_REPO_URL} target="_blank" rel="noreferrer">Source Code</a>
          </div>
        </div>

        <div className="footer-disclaimer">
          HACKATHON PROTOTYPE / SYNTHETIC DEMONSTRATION DATA
        </div>
      </footer>
    </div>
  );
}

// Helper component to render the exact pixel-perfect UI for each screen tab
function renderScreenUi(screenId) {
  switch (screenId) {
    case 'home':
      return (
        <div>
          <div className="phone-header">
            <div className="mono" style={{ fontSize: '11px', fontWeight: 'bold' }}>SCAMSHIELD X</div>
            <div className="phone-status-dot"><span className="dot-active"></span></div>
          </div>
          <div className="phone-body">
            <div className="phone-tag">PRE-ACTION SECURITY</div>
            <div className="phone-title">PROTECTION<br />ACTIVE</div>
            <div className="phone-quote">"Check before you click.<br />Check before you pay.<br />Check before you trust."</div>
            <div className="phone-divider"></div>
            <div className="phone-tool-row"><span>01 SCAN QR</span><span>→</span></div>
            <div className="phone-tool-row"><span>02 CHECK LINK</span><span>→</span></div>
            <div className="phone-tool-row"><span>03 ANALYZE TEXT</span><span>→</span></div>
          </div>
        </div>
      );
    case 'qr':
      return (
        <div>
          <div className="phone-header">
            <div className="mono" style={{ fontSize: '11px', fontWeight: 'bold' }}>01 / QR SCANNER</div>
            <div className="mono" style={{ fontSize: '9px', color: '#00E5FF' }}>CAMERA LIVE</div>
          </div>
          <div className="phone-body" style={{ textAlign: 'center', paddingTop: '20px' }}>
            <div style={{ border: '2px dashed #00E5FF', padding: '30px', margin: '10px 0' }}>
              <span style={{ fontSize: '32px' }}>📷</span>
              <div className="mono" style={{ fontSize: '10px', color: '#00E5FF', marginTop: '8px' }}>FRAME UPI / WEB QR</div>
            </div>
            <div className="phone-quote">"UPI URI parsed locally before payment confirmation."</div>
          </div>
        </div>
      );
    case 'link':
      return (
        <div>
          <div className="phone-header">
            <div className="mono" style={{ fontSize: '11px', fontWeight: 'bold' }}>02 / LINK ANALYSIS</div>
            <div className="mono" style={{ fontSize: '9px', color: '#FF2A2A' }}>HIGH RISK</div>
          </div>
          <div className="phone-body">
            <div className="mono" style={{ fontSize: '10px', color: '#737373' }}>ANALYZED URL:</div>
            <div className="mono" style={{ fontSize: '11px', color: '#FF2A2A', fontWeight: 'bold' }}>https://github.com.verification.invalid</div>
            <div className="phone-divider"></div>
            <div className="mono" style={{ fontSize: '10px', color: '#FF2A2A' }}>⚠ BRAND IMPERSONATION</div>
            <div className="mono" style={{ fontSize: '10px', color: '#FF2A2A' }}>⚠ HIGH-RISK SUSPICIOUS TLD</div>
          </div>
        </div>
      );
    case 'text':
      return (
        <div>
          <div className="phone-header">
            <div className="mono" style={{ fontSize: '11px', fontWeight: 'bold' }}>03 / TEXT ANALYSIS</div>
            <div className="mono" style={{ fontSize: '9px', color: '#00E5FF' }}>ON-DEVICE ML</div>
          </div>
          <div className="phone-body">
            <div className="mono" style={{ fontSize: '10px', color: '#94A3B8' }}>"Pay ₹999 registration fee to confirm interview..."</div>
            <div className="phone-divider"></div>
            <div className="mono" style={{ fontSize: '11px', color: '#FF2A2A', fontWeight: 'bold' }}>92 / 100 CRITICAL</div>
            <div className="mono" style={{ fontSize: '10px', color: '#FF2A2A' }}>RECRUITMENT FEE DEMAND</div>
          </div>
        </div>
      );
    case 'notif':
      return (
        <div>
          <div className="phone-header">
            <div className="mono" style={{ fontSize: '11px', fontWeight: 'bold' }}>04 / NOTIFICATION SHIELD</div>
            <div className="phone-status-dot"><span className="dot-active"></span></div>
          </div>
          <div className="phone-body">
            <div className="mono" style={{ fontSize: '10px', color: '#00E676', fontWeight: 'bold' }}>LISTENER CONNECTED ●</div>
            <div style={{ background: '#171717', border: '1px solid #FF2A2A', padding: '10px', marginTop: '10px' }}>
              <div className="mono" style={{ fontSize: '9px', color: '#FF2A2A' }}>🚨 POTENTIAL FRAUD DETECTED</div>
              <div className="mono" style={{ fontSize: '10px', color: '#ffffff', marginTop: '4px' }}>Bank account will be blocked...</div>
            </div>
          </div>
        </div>
      );
    case 'history':
      return (
        <div>
          <div className="phone-header">
            <div className="mono" style={{ fontSize: '11px', fontWeight: 'bold' }}>05 / THREAT HISTORY</div>
            <div className="mono" style={{ fontSize: '9px', color: '#94A3B8' }}>LOG (3)</div>
          </div>
          <div className="phone-body">
            <div style={{ borderBottom: '1px solid #1E293B', padding: '6px 0' }}>
              <div className="mono" style={{ fontSize: '10px', color: '#FF2A2A' }}>HIGH RISK • 92/100</div>
              <div className="mono" style={{ fontSize: '9px', color: '#737373' }}>BANKING PHISHING</div>
            </div>
            <div style={{ borderBottom: '1px solid #1E293B', padding: '6px 0' }}>
              <div className="mono" style={{ fontSize: '10px', color: '#FFB300' }}>SUSPICIOUS • 60/100</div>
              <div className="mono" style={{ fontSize: '9px', color: '#737373' }}>RECRUITMENT</div>
            </div>
          </div>
        </div>
      );
    case 'settings':
      return (
        <div>
          <div className="phone-header">
            <div className="mono" style={{ fontSize: '11px', fontWeight: 'bold' }}>06 / SETTINGS & ML LAB</div>
          </div>
          <div className="phone-body">
            <div className="mono" style={{ fontSize: '10px', color: '#00E5FF' }}>OFFICE KIT CONSOLE SYNC ●</div>
            <div className="mono" style={{ fontSize: '9px', color: '#737373', marginTop: '4px' }}>Target: http://10.0.2.2:8085</div>
            <div className="phone-divider"></div>
            <div className="mono" style={{ fontSize: '10px', color: '#00E676' }}>ML TENSOR PREDICTOR ACTIVE</div>
          </div>
        </div>
      );
    default:
      return null;
  }
}
