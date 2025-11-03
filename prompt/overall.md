# Hakki Wallet

## 项目定位
我们是浙大城市学院 软件工程专业的开发小组，我们是"8ga Bao"，"Kawaii"，"Hakki CLoud"和"YZY"。我们需要内开发一款记账软件，我们命名为Hakki Wallet。

## Vibe Coding
我们寻找了一个开源的项目作为我们的原型，并在上面继续做开发。我们所采用的开发方式主要是Vibe Coding，在后续的prompt中，无论你是Github Copilot、Trae，也无论你的模型是DeepSeek，claude或者是chatgpt，我们统一以Agent代称

## 开发的程序
1. 不同的开发者使用包含vscode，trae，cursor在内的Agent power的编辑器做vibe coding，并使用Andriod stdio编译，构建，预览等操作

## 资源
1. ``./code``文件夹下有所有需要的代码
2. ``./docs``文件夹下,agent需要维护``engineer.csv``，具体要求见后文。
3. ``./prompt``文件夹下有vibe coding中我们编写的部分重要提示词，``overall.md``即本文内容，综述了本项目，如果agent需要整理上下文或者重建对项目的认识，可以重新阅读本文件。
4. ``README``面向用户和社区开发者，由 agent 在开发中更新和维护

## 要求：
1. 原程序的技术栈已经很全面，请保持原程序所使用的技术栈。
2. 本APP是一个面向中文社区和用户的项目，所有的用户界面都需要使用中文。但开发的代码请使英文来撰写，包含英文命名的函数，方法等，代码中的注释请使用中文。
3. 你需要维护``docs/engineer.csv``，表头已经在csv中预先设定，只允许添加或改动记录而不得删除记录。而且这个记录应当详细，每一个改动和操作后都写一下记录，不要一次性多个改动后才更新一次记录。因为网络很容易断联，要确保新的 agent 能无缝接手每一次工作。engineer.csv要用中文写。其中的Operator的值填写("8ga Bao"，"Kawaii"，"Hakki Cloud"或者"YZY")，如果你不清楚目前的操作人是谁，请先提问。