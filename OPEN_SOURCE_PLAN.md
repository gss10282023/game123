# 开源改造与“去作业化”修改计划（Pacman JavaFX）

> 目标：把当前仓库从“课程作业代码”改造成一个**可以完整开源、可长期维护、可被他人复用**的 JavaFX 小游戏项目；同时在 GitHub README 的“简介区”就能直观看到**前端/界面效果（截图或 GIF）**。

## 0. 现状快速盘点（基于当前仓库）

- 技术栈：Java + Gradle + JavaFX（`build.gradle` 启用 `org.openjfx.javafxplugin`）
- 入口：`src/main/java/pacman/App.java`
- 资源：`src/main/resources/maze/*`（字体、墙体、精灵图等），`config.json` 与地图文件
- 明显的“不可发布/不利于开源复用”点：
  - 多处硬编码本地路径（例如 `src/main/resources/...`），打包成 JAR/发行版后会失效
  - 仓库缺少开源必需的“工程化配套”：License、第三方资源声明、Gradle Wrapper、CI、贡献指南等
  - README 偏“作业报告式”（主要写了 Pattern），缺少项目定位、功能亮点、演示效果、运行/打包说明

---

## 1. 最终交付物（开源发布要具备的最小集合）

建议把下面这些作为“开源完成”的验收清单（P0 必做，P1/P2 可选）：

### P0（必须）

- `README.md`：项目定位 + 功能介绍 + 运行方式 + **Demo 截图/GIF**（GitHub 直接可见）
- `LICENSE`：明确代码许可（MIT / Apache-2.0 / GPL-3.0 等择一）
- `THIRD_PARTY_NOTICES.md`（或 `ATTRIBUTION.md`）：字体/图片/依赖库的来源与许可证
- Gradle Wrapper：`gradlew`、`gradlew.bat`、`gradle/wrapper/*`（避免要求用户全局安装 Gradle）
- `.gitignore`：避免提交编译产物、IDE 文件
- 可复现运行：从干净环境 clone 后能 `./gradlew run` 启动

### P1（建议）

- `.github/workflows/ci.yml`：CI 自动构建 + 测试（至少 Linux/Windows）
- `CHANGELOG.md`：版本记录（哪怕从 `0.1.0` 开始）
- `CONTRIBUTING.md`：开发/提交流程与风格约定
- `CODE_OF_CONDUCT.md`：社区行为准则（若希望开放贡献）

### P2（可选增强）

- Release 发行物：`distZip`/`installDist` 或 `jpackage` 产物（可下载即用）
- Issue/PR 模板：`.github/ISSUE_TEMPLATE/*`、`PULL_REQUEST_TEMPLATE.md`
- `SECURITY.md`：安全漏洞报告渠道（如果要长期维护）

---

## 2. 详细实施步骤（可按阶段推进）

> 建议每个阶段都产出一个可运行/可展示的“里程碑”，避免一次性大改造成返工。

### Phase A：去作业化（项目身份与叙事重建）

**目标**：让仓库看起来像一个“作品/项目”，而不是“作业提交物”。

1) 重新命名与定位（先定“对外叙事”）
- 确定项目名（仓库名/README 标题/发行物名统一）
  - 示例：`PacmanFX` / `javafx-pacman` / `pacman-design-patterns`
- 写一句话定位（GitHub About/README 第一屏使用）
  - 示例：`A JavaFX Pac-Man clone showcasing Strategy/State/Decorator patterns.`
- 明确受众与价值
  - “想看效果的人”：直接看 GIF/截图
  - “想学习的人”：架构与 Pattern 说明、可扩展点
  - “想跑起来的人”：一条命令运行

2) 清理“作业痕迹”信息（面向公开仓库）
- 全仓库检索并移除/替换：
  - 作业编号（如 `a3`）、课程/老师/学号/提交说明、评分标准等
- 若 commit 历史包含作业信息（可选）：
  - 方案一（推荐）：新建公开仓库并做一次“干净初始提交”（更省心）
  - 方案二：rebase/squash 清理历史（风险更高，需确认不会影响协作）

3) 统一工程元信息
- 新增 `settings.gradle`：设置 `rootProject.name`
- 更新 `build.gradle`：
  - `group`、`version` 改为可公开发布的命名（例如 `io.github.<yourname>`）
  - `application.mainClass` 保持正确

**里程碑验收（Phase A Done）**
- GitHub About 和 README 第一屏不再像作业说明，能讲清“这是什么、有什么用、怎么跑、效果如何”

---

### Phase B：可运行性与可发布性（从“本地工程”变成“可分发项目”）

**目标**：任何人 clone 后无需手动改路径即可运行；打包后依然能运行。

1) 引入 Gradle Wrapper（P0）
- 本地执行一次生成（提交生成文件到仓库）：
  - `gradle wrapper --gradle-version <版本>`
- README 改用 `./gradlew ...` 而不是要求用户安装 Gradle

2) 修复硬编码路径（P0，关键）
- 目前存在类似：
  - `new GameEngineImpl("src/main/resources/config.json")`
  - `new File("src/main/resources/maze/PressStart2P-Regular.ttf")`
  - `config.json` 里写了 `src/main/resources/new-map.txt`
- 改造原则：
  - **所有资源走 classpath**（`src/main/resources` 打包进 JAR 后用 `getResourceAsStream` 读取）
  - 配置文件里不要写 `src/main/resources/...` 这种开发期路径，改成相对资源路径（例如 `new-map.txt` 或 `maze/maps/new-map.txt`）
- 建议落地方式（示例思路）：
  - `GameConfigurationReader` 支持从 `InputStream/Reader` 读取
  - `MazeCreator` 支持从 `InputStream` 读取地图
  - `GameWindow` 字体加载使用 `Font.loadFont(App.class.getResourceAsStream("/maze/PressStart2P-Regular.ttf"), size)`

3) 运行体验优化（P1）
- 将“配置错误时 `System.exit(0)`”改成抛出异常 + 清晰错误信息（对开源用户更友好）
- 增加 `--config <path>`（可选）：允许用户加载自定义配置（更像“开源项目”而不是固定作业输入）

4) 打包与发行（P1/P2）
- 基础：确保 `./gradlew build` 产物可运行
- 推荐：
  - `./gradlew installDist` / `distZip`：生成可分发目录/压缩包
  - （可选）`jpackage`：生成平台原生安装包

**里程碑验收（Phase B Done）**
- 从干净 clone 开始：`./gradlew run` 可启动
- 打包后（JAR 或 dist）无需依赖源码目录结构也能启动（不再依赖 `src/main/resources` 路径）

---

### Phase C：版权/许可与第三方资源合规（完整开源必经）

**目标**：代码与资源都能“合法开源”，避免后续被投诉或下架。

1) 选择项目代码许可证（P0）
- MIT：最宽松，适合“作品展示/教学示例”
- Apache-2.0：更完整的专利条款，偏工程
- GPL：强传染，除非明确希望如此，一般不建议作为游戏示例默认选

2) 审核第三方资源（P0）
- 重点检查 `src/main/resources/maze/**`（字体、精灵图、墙体图、pellet 图等）
- 对每个资源记录：
  - 来源链接 / 作者
  - 许可证类型（OFL / CC-BY / CC0 / 自绘等）
  - 是否允许二次分发与商用
- 如果资源来源不明或版权风险高：
  - 替换为可开源资源（OpenGameArt、Kenney、Google Fonts 等）或自己重绘

3) 添加声明文件（P0）
- `THIRD_PARTY_NOTICES.md` / `ATTRIBUTION.md`：
  - 字体：例如 Press Start 2P（若确认来源为 Google Fonts，附 OFL 说明与链接）
  - 图片/音效：逐项列出来源与许可证
  - 依赖库：如 `json-simple`（列许可证与链接）
- （可选）商标免责声明：
  - `Pac-Man` 属于 Bandai Namco 等持有方；本项目为粉丝/学习用途的克隆实现，不与官方关联

**里程碑验收（Phase C Done）**
- `LICENSE` + `THIRD_PARTY_NOTICES.md` 就位
- 资源可追溯、可分发（或已替换）

---

### Phase D：README “第一眼可见效果”（GitHub 上能直接看到前端效果）

**目标**：打开仓库首页就看到效果图（截图/GIF），并能快速理解怎么玩、怎么跑。

1) README 结构重写（P0）
- 推荐大纲（从“展示”到“细节”）：
  1. 项目标题 + 一句话定位
  2. Demo（GIF）/截图（放在最前面）
  3. Features（亮点：AI、关卡、配置化、模式切换等）
  4. Controls（方向键等）
  5. Quick Start（`./gradlew run`）
  6. Configuration（如何改 `config.json`/地图）
  7. Architecture / Patterns（把“作业 Pattern”转换成“工程设计说明”）
  8. License & Attribution

2) 产出可视化素材（P0）
- 截图：用于 README 静态展示
- GIF：用于“第一屏效果展示”（推荐 5–10 秒循环）
- 文件放置建议：
  - `docs/assets/demo.gif`
  - `docs/assets/screenshot-1.png`
- README 引用方式（相对路径即可，GitHub 直接渲染）：
  - `![Demo](docs/assets/demo.gif)`

3) GitHub 仓库设置（P1）
- About（右侧）设置：
  - Description：一句话定位
  - Topics：`java` `javafx` `game` `pacman` `design-patterns`
  - Website：可填 Release 下载页或演示视频链接（如果有）
- Social Preview（可选）：
  - 上传一张 1280×640 的预览图（分享链接时更好看）

**里程碑验收（Phase D Done）**
- GitHub 首页 README 第一屏可看到 Demo GIF/截图

---

### Phase E：工程质量与社区化（建议，但可按需）

1) 最小化测试（P1）
- 配置读取：`GameConfigurationReader` 的 parse/错误处理
- 地图解析：`MazeCreator` 对非法字符/行宽的行为
- 关键逻辑：Ghost 策略/状态切换（至少做 1–2 个单元测试示例）

2) CI（P1）
- GitHub Actions：`./gradlew test` + `./gradlew build`
- 缓存 Gradle，提高速度

3) 贡献与协作（P1/P2）
- `CONTRIBUTING.md`：如何本地运行、如何提交 PR、代码风格
- Issue/PR 模板：让外部贡献更顺畅

---

## 3. 总体验收标准（Definition of Done）

- 从零开始：
  - `git clone` → `./gradlew run` 一次成功
- 不依赖源码路径：
  - 不再出现运行时读取 `src/main/resources/...` 的硬编码
- GitHub 展示到位：
  - README 第一屏有 Demo GIF/截图 + 清晰的 Quick Start
- 合规：
  - `LICENSE` + 第三方资源/依赖声明完整

---

## 4. 建议推进顺序（最省时间的路线）

1. Phase D（先把 README + Demo 做出来，马上“像项目”）
2. Phase B（把路径与 wrapper 修好，确保别人能跑）
3. Phase C（做资源与许可审计，避免开源风险）
4. Phase A（完善命名/叙事，必要时清理历史）
5. Phase E（CI/测试/社区化按需补齐）

