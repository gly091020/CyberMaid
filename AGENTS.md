# Repository Guidelines

## 规范补充

- 回答与提交信息统一用中文。
- 注释从简：只在 `api/` 包写 `/**` 文档注释，其余代码不复述逻辑。
- 数据文件（配方等）里能换成标签的物品一律用标签，如 `minecraft:iron_ingot` → `c:ingots/iron`、`minecraft:white_wool` → `minecraft:wool`。
- 配方、物品模型、标签一律由数据生成产出，不要手写 json；语言文件例外，`assets/cyber_maid/lang/` 保持手写维护。
- CyberWare Port 与 Touhou Little Maid 是前置模组，不修改其源码，只通过 API、事件与 mixin 扩展。
- 改动集中在 `com.gly091020.CyberMaid` 与本仓库资源目录，不顺手改动无关文件。

## Project Structure & Module Organization

- `src/main/java/com/gly091020/CyberMaid/` — 模组源码，按职责分包：`api`（扩展接口）、`client`、`datagen`（数据生成 provider）、`event`（事件入口）、`network`（自定义网络包）、`util`（非玩家实体的能力、同步与手术处理，统一 `*WithoutPlayer` 后缀）、`mixin`（含 `accessor/`、`cyberware/`）。
- `src/main/resources/` — 手写资源：`assets/cyber_maid/lang/`（`en_us.json`、`zh_cn.json`）与 `cyber_maid.mixins.json`。
- `src/generated/resources/` — 数据生成产物（配方、物品模型、标签），随源码一起打包，不要手改。
- `src/main/templates/META-INF/neoforge.mods.toml` — 构建时由 `generateModMetadata` 展开的模组元数据。
- `run/` — 本地开发运行目录，不提交。

## 架构要点

- 女仆等非玩家实体走 `util/HandleCyberware*WithoutPlayer` 这套镜像逻辑，不要改前置模组的玩家路径。
- 部件的每 tick 行为实现 `MaidCyberwareTick.onMaidTick`，由 `HandleCyberwareUserDataWithoutPlayer.tick` 分发；属性与能量容量重建放在 `recalculateCapacity`。
- 事件类只负责分发（`HandleCyberwareEventsWithoutPlayer.dispatch`），具体行为写在部件自身。
- 新增 mixin 必须登记到 `cyber_maid.mixins.json` 的 `mixins` 或 `client` 数组，否则不会被应用。
- 新增“赛博”生物要实现 `ICyberwareMob`，掉落交给前置模组的管线（普通池 + `getSpecialDrops()`，掉落物会被标成非全新）。

## Build, Test, and Development Commands

构建系统为 NeoForge `net.neoforged.moddev`，Minecraft 1.21.1、Java 21。

- `.\gradlew.bat compileJava --console=plain -q` — 快速编译源码。
- `.\gradlew.bat build` — 完整构建，产物在 `build/libs/cyber_maid-<version>.jar`。
- `.\gradlew.bat runClient` / `.\gradlew.bat runServer` — 启动开发客户端/服务端。
- `.\gradlew.bat runData` — 数据生成，输出到 `src/generated/resources/`；改完 `datagen` 下的 provider 后必须重跑，生成物与 `<item>_assembly` / `<item>_engineering` 等命名保持一致。

前置依赖从 Modrinth maven 拉取，首次构建需要网络。

## Coding Style & Naming Conventions

- Java 21，4 空格缩进，不使用 tab，使用官方 Mojang 映射。
- 包名小写；类用 PascalCase，方法与字段 camelCase，常量 `UPPER_SNAKE_CASE`；mixin 以 `XxxMixin`、accessor 以 `XxxAccessor` 命名。
- 注册 ID 与资源路径用 snake_case 并以 `cyber_maid` 为命名空间；语言键形如 `item.cyber_maid.<id>`、`cyberware.tooltip.<id>`。
- 未配置格式化或 lint 工具，保持与相邻代码一致，控制单次改动范围。
- **注释从简**：只在对外 API 上写 `/** */`；内部逻辑靠命名表达，不写"这行在做什么"；只有"为什么"不明显时才留一行注释。

## Testing Guidelines

仓库没有 `src/test` 目录，也没有 CI。agent 只做 `compileJava` / `build` / `runData` 自检，**不要启动 `runClient` / `runServer` 做实机验证**——游戏内实测由作者本人进行。

## Commit & Pull Request Guidelines

- 提交信息是简短中文描述，如 `女仆专用部件完善`、`一些更改`，一次提交只做一件事。
- PR 需说明改了什么、为什么改、游戏内验证了哪些场景，并关联相关 issue。
