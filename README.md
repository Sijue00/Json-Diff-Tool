前端由Vue3 构建，拉完源码直接 npm install 下载相关依赖，之后npm run dev开启，默认端口为3000。
后端由Springboot构建，Java 17 Maven 3.8开发，Maven拉完Pom文件依赖即可开启。
页面如图：
<img width="2552" height="1308" alt="1" src="https://github.com/user-attachments/assets/44b02d4e-534c-49fe-ac81-23c3d511b515" />
页面功能简单易懂，提供了基础Json数据和多级Json数据进行快速测试
<img width="2552" height="1308" alt="3" src="https://github.com/user-attachments/assets/633d9eeb-2a8a-4a6c-b889-21e552fa9212" />
编辑器基于monaco-editor 实现，以及树结构差异点击移动到到编辑器高亮显示
<img width="2552" height="1308" alt="4" src="https://github.com/user-attachments/assets/cd5c54f8-1ec4-4996-9822-d7e4775354a8" />
支持四种差异化导出查看，且可以在线预览


后端核心差异比较算法使用了深度优先递归处理

