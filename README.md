# 个人效率计算 Android

当前版本：V1.1.1（versionCode 3）

本版本新增 Excel 批量导入项目/工序、项目/工序 Excel 备份导出、完整 JSON 数据备份导出；保留原有单个添加项目/工序功能。项目识别规则为“一个工作表对应一个项目”，工作表名称作为项目名称，自动识别“序号 / 工序 / M-MIN”表头。


Android WebView 离线效率统计工具，版本 **v1.1.0**。安卓端与网页模拟共用同一套前端源码，确保功能和视觉一致。

## 功能

- 自定义项目名称、单位和每分钟标准产量
- 录入和编辑每次工作的时长与实际产量
- 自动计算单项目效率，支持超过 100%
- 按天汇总多个项目，并按工时计算加权平均效率
- 今日时间分布、工作记录、日效率趋势和项目效率对比
- SQLite 本机离线保存，无需账号
- 设置页显示 APP 版本，并预留云同步入口
- 浏览器和 Android WebView 均使用全宽全高自适应布局
- 首页时间轴根据当天最早开始和最晚结束时间动态伸缩，支持晚间及跨日记录

## 计算方式

单项目效率 = 实际产量 ÷（有效分钟数 × 每分钟标准产量）× 100%

日平均效率 = Σ（项目效率 × 项目工时）÷ Σ项目工时

## 构建

使用 Android Studio Ladybug 或更新版本打开本目录，安装 Android SDK 35 后：

```bash
./gradlew assembleDebug
```

生成位置：`app/build/outputs/apk/debug/app-debug.apk`

## 操作提示

- “今日”页点击“记录项目”新增工作记录。
- “记录”页点击记录可编辑，长按可删除。
- “项目”页点击项目可编辑，长按可删除。
- 首次启动自带演示项目和当天两条演示记录，便于验证界面。

## 网页交互模拟

打开 `web-demo/index.html` 即可在浏览器中运行，不需要安装依赖或启动服务器。Android 使用标准的 `app/src/main/assets/` 目录保存同一套前端文件，入口 Activity 通过 `file:///android_asset/index.html` 加载。

为提高 Android 7.0 及以上系统的兼容性，启动类不直接引用 Android 11 才新增的系统栏 API；如果设备缺少 Android System WebView，应用会显示明确提示而不是直接闪退。

## 本次更新
- 内置《项目工序表.xlsx》中的 5 个项目、87 道工序（原项目 + 新增 4 个项目）。
- 添加工序时先选择项目，工序列表按项目过滤。
- 添加工序页增加“常用工序”，按历史使用次数自动排序，点击即可快速选择。
- 今日记录显示所属项目，并支持修改项目/工序/数量。
- 工序管理按项目分组显示。
- Android 原生实现数据导出/导入文件选择器，解决 WebView 中点击无反应的问题。
- 分享仍直接调用系统分享，不提供保存图片到手机的选项。


## v1.1.0 优化
- 今日记录项目名称字号恢复并增强可读性。
- 页面返回改为基于 History 的上一级返回，避免“今天/日历”返回链断开。
- 自动迁移旧版本 localStorage：补齐全部 5 个项目及缺失的默认工序。
- 添加工序中的“常用工序”独立成块，并支持跨项目快速选择。
- 工序管理改为“先显示项目，点击项目再展开工序”的折叠结构。


v11 updates: optimized direct system image sharing (JPEG), added project creation in Process Management, whole-row record editing except delete, long process names wrap fully, and synchronized default process data with the supplied project table.
