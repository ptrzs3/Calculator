# Calculator (Android)

一个基于 **Jetpack Compose** 构建的 Android 计算器应用，提供标准、科学和程序员三种计算模式，交互风格参考 Microsoft Calculator。

<center>
<img src="https://pics-1300084735.cos.ap-nanjing.myqcloud.com/Screenshot_20260417_154454.jpg" alt="Screenshot_20260417_154454" style="zoom:20%;"/>
<img src="https://pics-1300084735.cos.ap-nanjing.myqcloud.com/Screenshot_20260417_154505.jpg" alt="Screenshot_20260417_154505" style="zoom:20%;"/>
</center>

## 项目简介

本项目目标是在 Android 上实现一款日常可用、功能完整、视觉风格统一的多模式计算器。

- 支持标准计算、科学计算、程序员计算三种模式
- 支持浅色/深色主题切换
- 支持表达式显示与结果显示分离
- 支持长按结果复制到剪贴板

## 设计与实现声明

- 本 APP 的设计风格仿照 **Microsoft Calculator**。
- 本 APP 的设计与实现由 **Codex** 和 **Claude** 协作完成，并由人类进行监督与决策。
- 本项目仅用于学习与技术交流，不代表与 Microsoft 官方存在关联。

## 功能说明

### 1) 标准模式 (Standard)

- 四则运算：`+` `-` `×` `÷`
- 百分比 `%`
- 清除相关：`C`、`CE`、退格
- 一元运算：`1/x`、`x²`、`√x`
- 数值输入：正负号切换、小数点、连续计算

### 2) 科学模式 (Scientific)

- 基础科学函数：`sin` `cos` `tan` `ln` `log`
- 扩展三角函数：`sec` `csc` `cot`
- `2nd` 切换：平方/立方、平方根/立方根、幂/开方、`10^x`/`2^x`、`log`/`log_y(x)`
- 三角菜单支持：反三角 (`sin⁻¹` 等)、双曲 (`sinh` 等)、反双曲
- 其他函数：`|x|`、`floor`、`ceil`、`rand`、`n!`、`exp`
- 表达式能力：括号、`mod`、幂运算、`yroot`、`logbase`、混合表达式求值
- 角度转换：`→dms`、`→deg`

### 3) 程序员模式 (Programmer)

- 进制切换：`HEX` / `DEC` / `OCT` / `BIN`
- 字长切换：`QWORD(64)` / `DWORD(32)` / `WORD(16)` / `BYTE(8)`
- 位运算：`AND` `OR` `NOT` `NAND` `NOR` `XOR`
- 位移运算：`<<` `>>`
- 位移模式：算术移位、逻辑移位、循环移位、带进位循环移位
- 输入面板切换：按键输入 / 位图输入（可直接点击 bit 位）

## 技术栈

- Kotlin
- Jetpack Compose (Material 3)
- Android Gradle Plugin `9.1.1`
- Kotlin Compose Plugin `2.2.10`
- Min SDK `24`
- Target SDK `36`

## 运行方式

1. 使用 Android Studio 打开项目目录。
2. 等待 Gradle 同步完成。
3. 选择模拟器或真机。
4. 运行 `app` 模块。

## 运行模式

### 标准模式

<img src="https://pics-1300084735.cos.ap-nanjing.myqcloud.com/Screenshot_20260417_154454.jpg" style="zoom:20%;" />

### 科学模式

<img src="https://pics-1300084735.cos.ap-nanjing.myqcloud.com/Screenshot_20260417_154456.jpg" alt="Screenshot_20260417_154456" style="zoom:20%;" />

### 程序员模式

<img src="https://pics-1300084735.cos.ap-nanjing.myqcloud.com/Screenshot_20260417_154458.jpg" alt="Screenshot_20260417_154458" style="zoom:20%;" />

## 项目结构

```text
app/src/main/java/com/example/myapplication/
├── model/       # 状态与动作定义
├── reducer/     # 计算与状态归约逻辑
├── ui/          # Compose 页面与组件
└── viewmodel/   # 状态管理与事件分发
```
