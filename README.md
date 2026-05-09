# MDT Force Jump

`mdt-force-jump-mdt` 是一个独立的入服强跳插件。

玩家加入服务器后，插件会按配置构造 `mdt://redirect` 协议内容，并通过消息和断开原因发送给客户端。

## 说明

- 兼容 `MDT redirect` 协议的客户端可以据此自动跳转
- 原版客户端不会原生自动换服，只会看到提示或断开信息
- 跳转目标地址、端口、显示名、是否断开都可以在配置中修改

## 命令

- `force-jump-reload`
- `force-jump-status`
- `force-jump-send <player>`

## 配置文件

首次启动后会自动生成：

```text
config/mods/config/mdt-force-jump-mdt/force-jump-mdt.properties
```

## 插件入口

```text
com.mdt.forcejump.ForceJumpMdtPlugin
```
