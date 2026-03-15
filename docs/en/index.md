---
layout: home

hero:
  name: TalkHub
  text: Docs and Local Onboarding
  tagline: Documentation for the current mock-enabled build. Start from a working local demo, then trace how Spring Boot, Netty, the browser adapter layer, and persistence fit together.
  actions:
    - theme: brand
      text: Launch Mock in 3 Minutes
      link: /en/guide/mock-quickstart
    - theme: alt
      text: Read Architecture
      link: /en/guide/architecture
    - theme: alt
      text: General Setup
      link: /en/guide/getting-started

features:
  - title: Shortest startup path
    details: Go from .env and PostgreSQL to make mock with the minimum dependency surface.
  - title: Current transport story
    details: Understand the browser /api/im/browser bridge first, then separate that from the Netty long-connection main path.
  - title: Product-facing docs
    details: Track login updates, history loading, channel flow, and local verification from one place.
---

<div class="home-panel">
  <h2>Recommended reading order</h2>
  <p>If this is your first pass through the current version, get the mock build running first, then read the architecture and behavior notes. That sequence is faster and produces fewer wrong assumptions.</p>
  <div class="home-grid">
    <div class="home-grid-item">
      <strong>1. Run the app</strong>
      <span>Start with <a href="/en/guide/mock-quickstart">Mock Quickstart</a> for the exact local commands, seeded accounts, and first-line troubleshooting.</span>
    </div>
    <div class="home-grid-item">
      <strong>2. Build the system map</strong>
      <span>Read <a href="/en/guide/architecture">Architecture</a> to anchor the roles of Spring Boot, Netty, Redis, RabbitMQ, and PostgreSQL.</span>
    </div>
    <div class="home-grid-item">
      <strong>3. Match shipped behavior</strong>
      <span>Use <a href="/en/reference/login-update">Login Update</a> to understand what the frontend login flow already supports.</span>
    </div>
  </div>
</div>
