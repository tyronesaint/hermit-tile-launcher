# Hermit Tile Launcher

Android 控制中心快捷方式管理器，点击即可秒开 Hermit 轻应用。

## 功能特性

- 4 个可自定义的快速设置磁贴
- 磁贴默认灰色关闭状态
- 点击磁贴直接打开 Hermit 浏览器
- 自定义图标支持
- Material Design 3 高颜值配置页面
- GitHub Actions 自动编译 APK

## 使用方法

### 1. 配置快捷方式

1. 安装 APK 后打开应用
2. 输入**磁贴显示名称**
3. 输入 **Hermit 轻应用网址**
4. 点击**「自动生成Key」**按钮获取 Key
5. 点击**「保存」**

### 2. 添加磁贴到控制中心

1. 下拉通知栏
2. 点击编辑按钮（铅笔图标）
3. 找到 "Hermit 快捷方式" 磁贴
4. 拖动到控制中心
5. 完成配置

### 3. 使用

- 点击控制中心的磁贴即可秒开对应的 Hermit 轻应用
- 如需修改配置，打开应用重新编辑即可

## Key 生成规则

Hermit Key 格式：`hostname-8位随机字符`

例如：
- 网址：`https://m.baidu.com/`
- 生成的 Key：`m.baidu.com-a1b2c3d4`

每次点击「自动生成Key」都会生成不同的随机后缀。

## 项目结构

```
hermit-tile-launcher/
├── app/
│   ├── src/main/
│   │   ├── java/com/shortcuts/tilelauncher/
│   │   │   ├── TileConfig.kt         # 配置数据类 + Key生成
│   │   │   ├── TileService1-4.kt     # 4个磁贴服务
│   │   │   └── MainActivity.kt       # 配置页面
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml
│   │   │   ├── values/colors.xml
│   │   │   ├── values/strings.xml
│   │   │   └── mipmap-*/           # 图标资源
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── .github/workflows/build.yml       # CI/CD 自动构建
├── build.gradle                      # 项目配置
├── settings.gradle
└── gradle.properties
```

## GitHub Actions 自动构建

推送到 main 分支后会自动构建：

1. 检出代码
2. 配置 JDK 17
3. 下载 Gradle Wrapper
4. 执行 `./gradlew assembleDebug`
5. 上传 APK 到 Artifacts

手动触发：在 GitHub Actions 页面点击 "Run workflow"

下载 APK：Actions → 构建任务 → Artifacts → HermitTile-APK

## 自定义图标

替换 `mipmap-*` 目录下的图标文件：

| 文件 | 说明 | 推荐尺寸 |
|------|------|----------|
| ic_tile1.png | 磁贴1图标 | 48x48 ~ 192x192 |
| ic_tile2.png | 磁贴2图标 | 同上 |
| ic_tile3.png | 磁贴3图标 | 同上 |
| ic_tile4.png | 磁贴4图标 | 同上 |
| ic_launcher.png | 应用图标 | 同上 |

## 技术栈

- Kotlin
- Android SDK 34
- Material Design 3
- Quick Settings Tile API
- Gradle 8.2

## Hermit 包名

- 包名：`com.chimbori.hermitcrab`
- Activity：`com.chimbori.hermitcrab.BrowserActivity`

## License

MIT
