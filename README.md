# 🎵 Rhythbox

**一款基于 Kotlin Multiplatform 和 Compose Multiplatform 开发的个人网络音乐播放器。**

Rhythbox 致力于打造一个简洁、流畅、支持本地音乐与网络音乐的跨平台播放器。

项目目前仍处于积极开发阶段，正在持续完善基础功能和整体架构。

> [!WARNING]
> **项目尚未完成，目前处于开发阶段！**
>
> - 部分功能尚未实现或仍在调整中。
> - 项目架构、数据库结构及 API 接口可能随时发生变化。
> - 目前不保证所有功能稳定运行，也不保证不同版本之间的数据兼容性。
> - 暂不建议用于生产环境。
>
> 欢迎 Star、Fork 和提出建议，但请谨慎用于日常使用。

---

## ✨ 项目介绍

Rhythbox 是一个个人音乐播放器项目，采用 Kotlin Multiplatform 技术构建，使用 Compose Multiplatform 实现跨平台 UI。

项目希望将本地音乐管理与网络音乐服务结合起来，提供统一的音乐播放体验。

主要设计方向：

- 🎧 支持本地音乐与网络音乐播放。
- 📂 本地音乐扫描及管理。
- ☁️ 网络歌曲、歌手、专辑和歌单管理。
- ❤️ 收藏歌曲及创建个人歌单。
- ⬇️ 音乐下载与下载任务管理。
- 🎼 歌词展示。
- 🎵 播放列表及播放状态管理。

以上为项目规划与开发方向，并不代表所有功能均已完成。

## 🛠️ 技术栈

| 技术 | 用途 |
|---|---|
| Kotlin Multiplatform | 跨平台开发 |
| Compose Multiplatform | UI 构建 |
| Voyager | 页面导航与状态管理 |
| Ktor | 网络请求 |
| Room | 本地数据库 |
| Kotlinx Serialization | 数据序列化 |
| Media3 / ExoPlayer | Android 音频播放 |
| Ketch | 下载任务管理 |
| Coil | 图片加载 |
| Koin | 依赖注入 |

## 📱 平台支持

| 平台 | 状态 |
|---|---|
| Android | 开发中 |
| Desktop | 规划中 |
| iOS | 规划中 |

平台支持状态会随着项目开发进度更新。



> 功能状态可能随开发进度调整，具体以仓库最新代码为准。

## ⚙️ 开发环境

开始开发前，请准备以下环境：

- JDK 11 或更高版本（以项目 Gradle 配置为准）。
- Android Studio。
- Kotlin Multiplatform 开发环境。
- Android SDK。

### 克隆项目

```bash
git clone https://github.com/你的用户名/Rhythbox.git

cd Rhythbox
```

### 配置服务器地址

项目需要配合对应的后端服务使用。

请根据实际开发环境配置音乐服务 API 地址，并检查项目中的环境配置。

例如：

```kotlin
buildConfig {
    packageName("wzy.hkta.rhythbox")

    buildConfigField(
        "MUSIC_BASE_URL",
        "http://你的服务器地址"
    )
}
```

### 编译运行

Android：

```bash
./gradlew :composeApp:assembleDebug
```

Desktop：

```bash
./gradlew :composeApp:run
```

具体运行方式以项目当前 Gradle 配置为准。

## 🖼️ 项目截图

项目 UI 仍在持续开发中，后续会在这里补充应用截图。

<!--
![首页](docs/images/home.png)
![播放页面](docs/images/player.png)
![歌单页面](docs/images/playlist.png)
-->

## 🤝 参与贡献

欢迎对项目感兴趣的开发者参与交流和贡献。

你可以通过以下方式参与：

- 提交 Issue 反馈问题或提出功能建议。
- Fork 项目并提交 Pull Request。
- 帮助完善文档及项目功能。

由于项目仍处于开发阶段，提交代码前建议先通过 Issue 讨论较大的架构调整。

## ⚠️ 免责声明

本项目仅用于个人学习、技术研究及交流。

- 本项目不提供音乐资源文件。
- 用户应确保自己拥有所使用音乐资源的合法授权。
- 请勿利用本项目传播、下载或分享未经授权的版权内容。
- 因使用者自身行为产生的版权或法律问题，由使用者自行承担。

## 📄 License

项目许可证暂待确定。

---

**Rhythbox — Enjoy Your