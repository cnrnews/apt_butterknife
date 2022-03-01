## apt_butterknife
手写Butterknife实现无性能损耗的编译时框架

### 项目说明
> app 应用程序主工程

> annotation
注解工程，声明常用的注解类型

> compiler
注解处理器工程，对工程注解进行解析，生成静态java文件

> library
辅助工程：使用生成的java文件，完成view注入和事件绑定

### 应用到的技术
1. 反射+注解
2. javapoet 生成java代码

### 参考文章
https://blog.csdn.net/CSDN_Mew/article/details/103880788?spm=1001.2014.3001.5502
