import { defineConfig } from "vitepress";

export default defineConfig({
  title: "TalkHub Docs",
  description: "TalkHub project documentation.",
  lang: "zh-CN",
  lastUpdated: true,
  locales: {
    root: {
      label: "简体中文",
      lang: "zh-CN",
      link: "/",
      title: "TalkHub 文档",
      description: "TalkHub 项目文档站点",
      themeConfig: {
        nav: [
          { text: "指南", link: "/guide/getting-started" },
          { text: "架构", link: "/guide/architecture" },
          { text: "登录更新", link: "/reference/login-update" }
        ],
        sidebar: [
          {
            text: "开始",
            items: [{ text: "快速开始", link: "/guide/getting-started" }]
          },
          {
            text: "设计",
            items: [{ text: "系统架构", link: "/guide/architecture" }]
          },
          {
            text: "参考",
            items: [{ text: "登录功能更新说明", link: "/reference/login-update" }]
          }
        ],
        socialLinks: [{ icon: "github", link: "https://github.com/" }]
      }
    },
    en: {
      label: "English",
      lang: "en-US",
      link: "/en/",
      title: "TalkHub Docs",
      description: "TalkHub documentation site",
      themeConfig: {
        nav: [
          { text: "Guide", link: "/en/guide/getting-started" },
          { text: "Architecture", link: "/en/guide/architecture" },
          { text: "Login Update", link: "/en/reference/login-update" }
        ],
        sidebar: [
          {
            text: "Start",
            items: [{ text: "Getting Started", link: "/en/guide/getting-started" }]
          },
          {
            text: "Design",
            items: [{ text: "Architecture", link: "/en/guide/architecture" }]
          },
          {
            text: "Reference",
            items: [{ text: "Login Update", link: "/en/reference/login-update" }]
          }
        ],
        socialLinks: [{ icon: "github", link: "https://github.com/" }]
      }
    }
  },
  themeConfig: {
    search: {
      provider: "local"
    }
  }
});
