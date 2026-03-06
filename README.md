# LLM 算力评估器 (LLM Capacity Estimator)

## 项目简介
LLM 算力评估器是一个全栈应用，旨在帮助用户精准评估部署大语言模型（LLM）所需的硬件资源（显存与算力）。通过输入目标模型名称和业务场景，系统利用 AI 智能提取模型参数，并基于严谨的数学公式计算不同并发下的资源需求，生成直观的可视化图表。

## 项目声明

1.以下内容全部由dhg完成，版权所有，允许自己部署，但是严禁商用转卖

2.算力评估方式为dhg个人对于该领域的理解，不代表专业评估，如因使用该方案评估造成的资源不足或浪费，概不负责

3.参数提取等由大模型完成，不确保提取一定正确，请主动核验

## 核心功能
1.  **智能参数提取**: 通过调用外部 LLM API (GLM-4)，自动从用户输入的模型名称（如 `cyankiwi/Qwen3.5-27B-AWQ-BF16-INT8`）中提取关键参数：
    *   模型总参数量 (Model Parameters)
    *   激活参数量 (Activated Parameters)
    *   量化精度 (Quantization Precision)
    *   量化因子 (Quantization Factor)
2.  **场景化性能目标推断**: 根据用户输入的场景描述（如“高并发实时客服”），自动推断合理的目标生成速度 (Tokens/s)。
3.  **多维资源估算**: 计算不同并发数（1-128）下的：
    *   显存需求 (VRAM GB)
    *   算力需求 (Compute TFLOPS)
4.  **可视化展示**: 使用 ECharts 渲染动态曲线图，直观展示资源随并发量的增长趋势。

## 计算原理说明

本项目采用以下核心公式进行资源估算：

### 1. 基础参数定义
*   **P_total**: 模型总参数量 (单位：Billion/十亿)
*   **P_active**: 激活参数量 (单位：Billion/十亿)
*   **Q_factor**: 量化因子 (单位：字节/Byte)。例如 INT8=1, FP16=2。
*   **C**: 并发数 (Concurrency)
*   **L_in**: 单次平均输入 Token 长度
*   **L_out**: 单次平均输出 Token 长度
*   **T_speed**: 目标生成速度 (Tokens/s)

### 2. 中间系数计算
*   **上下文系数 (K_context)**:
    用于估算 KV Cache 显存占用的经验系数，与模型规模呈正相关。
    
    K_context = 0.000003 * P_total + 0.0001

*   **输入输出比率因子 (R_io)**:
    
    R_io = 1 + (L_in / L_out)

### 3. 显存需求 (VRAM) 计算公式
显存主要由两部分组成：模型权重占用 + KV Cache (上下文) 占用。额外乘以 1.2 倍作为冗余（CUDA Context、碎片等）。

VRAM (GB) = (P_total * Q_factor + C * (L_in + L_out) * K_context) * 1.2

*   **P_total * Q_factor**: 静态模型权重显存。
*   **C * (L_in + L_out) * K_context**: 动态 KV Cache 显存。

### 4. 算力需求 (TFLOPS) 计算公式
算力估算基于激活参数和目标吞吐量，并考虑 Prefill (输入) 和 Decode (输出) 阶段的计算量。系数 0.005 是一个经验转换因子，将计算量映射到所需的 TFLOPS (FP16/BF16 精度)。

Compute (TFLOPS) = P_active * C * T_speed * R_io * 0.005

## 技术栈
*   **前端**: Vue 3, TypeScript, Vite, Tailwind CSS, ECharts
*   **后端**: Java 17, Spring Boot 3, LangChain4j
*   **AI 服务**: 接入 SiliconFlow API (Pro/zai-org/GLM-4.7)

## 快速开始
1.  配置后端环境变量 `LLM_API_KEY`。
2.  启动后端: `mvn spring-boot:run`
3.  启动前端: `npm run dev`
4.  访问 `http://localhost:9091`
