---
layout: home

hero:
  name: TalkHub
  text: 文档与本地上手入口
  tagline: 面向当前 mock 可运行版本的项目文档。先跑起来，再理解 Spring Boot、Netty、浏览器适配层和数据链路是怎么接上的。
  actions:
    - theme: brand
      text: 3 分钟跑起 Mock 版本
      link: /guide/mock-quickstart
    - theme: alt
      text: 看系统架构
      link: /guide/architecture
    - theme: alt
      text: 通用快速开始
      link: /guide/getting-started

features:
  - title: 最短启动路径
    details: 从 .env、PostgreSQL 到 make mock，按最少依赖面把当前版本跑起来。
  - title: 当前链路说明
    details: 先看浏览器如何通过 /api/im/browser 接入实时桥接，再区分 Netty 主链路与浏览器调试链路。
  - title: 关键改动留痕
    details: 登录更新、历史消息、频道体验和文档演进都可以直接在站点里追踪。
---

<div class="home-panel">
  <h2>推荐阅读顺序</h2>
  <p>如果你是第一次接手这个版本，先用 Mock 账号把页面跑起来，再看架构和接口边界。这样能最快建立对代码和运行面的真实认识。</p>
  <div class="home-grid">
    <div class="home-grid-item">
      <strong>1. 跑起来</strong>
      <span>从 <a href="/guide/mock-quickstart">Mock 版本快速上手</a> 开始，直接获得可登录环境、默认账号和排错入口。</span>
    </div>
    <div class="home-grid-item">
      <strong>2. 建立全局图</strong>
      <span>阅读 <a href="/guide/architecture">系统架构</a>，确认 Spring Boot、Netty、Redis、RabbitMQ 和 PostgreSQL 各自负责什么。</span>
    </div>
    <div class="home-grid-item">
      <strong>3. 对照功能面</strong>
      <span>查看 <a href="/reference/login-update">登录功能更新说明</a>，理解前端现阶段已经打通的登录与持久化能力。</span>
    </div>
  </div>
</div>
