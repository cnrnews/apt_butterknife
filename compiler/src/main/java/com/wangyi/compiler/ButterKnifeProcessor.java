package com.wangyi.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.wangyi.annotation.BindView;
import com.wangyi.annotation.OnClick;
import com.wangyi.compiler.utils.Constants;
import com.wangyi.compiler.utils.EmptyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Filter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import sun.util.resources.cldr.zh.CalendarData_zh_Hans_HK;

/**
 * @Author lihl
 * @Date 2022/2/27 17:00
 * @Email 1601796593@qq.com
 */

@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {

    // 操作 Element 工具类
    private Elements elementUtils;

    // type(类信息)工具类，包含用于操作 TypeMirror 的工具方法
    private Types typeUtils;

    // Messager 用来报告错误，警告和其他提示信息
    private Messager messager;

    // 文件生成器 ， Filter 用来创建新的类文件
    private Filer filter;

    // key: 类节点，value: 被 @BindView 注解的属性集合
    private Map<TypeElement, List<VariableElement>> tempBindViewMap = new HashMap<>();

    // key: 类节点，value: 被 @OnClick 注解的方法集合
    private Map<TypeElement, List<ExecutableElement>> tempOnClickMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        System.out.println("init..");
        // 初始化
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filter = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();

        messager.printMessage(Diagnostic.Kind.NOTE,
                "注解处理器初始化完成，开始处理注解...");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!EmptyUtils.isEmpty(set)) {
            //获取所有被 @BindView 注解的集合
            Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);

            //获取所有被 @OnClick 注解的集合
            Set<? extends Element> onClickElements = roundEnvironment.getElementsAnnotatedWith(OnClick.class);

            if (!EmptyUtils.isEmpty(bindViewElements) || !EmptyUtils.isEmpty(onClickElements)) {
                // 收集集合
                valueOfMap(bindViewElements, onClickElements);
                // 生成类文件
                try {
                    createJavaFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 收集被注解的信息到 map
     *
     * @param bindViewElements
     * @param onClickElements
     */
    private void valueOfMap(Set<? extends Element> bindViewElements, Set<? extends Element> onClickElements) {
        // 搜集 @BindView 注解信息
        if (!EmptyUtils.isEmpty(bindViewElements)) {
            for (Element element : bindViewElements) {
                messager.printMessage(Diagnostic.Kind.NOTE, "@BindView>>> " + element.getSimpleName());
                // 注解是否作用在属性上
                if (element.getKind() == ElementKind.FIELD) {
                    VariableElement variableElement = (VariableElement) element;
                    // 获取父节点，作为 tempBindViewMap 的 key
                    TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
                    // 包含key为typeElement的集合则直接添加
                    if (tempBindViewMap.containsKey(typeElement)) {
                        tempBindViewMap.get(typeElement).add(variableElement);
                    } else {
                        List<VariableElement> fields = new ArrayList();
                        fields.add(variableElement);
                        tempBindViewMap.put(typeElement, fields);
                    }
                }
            }
        }

        // 搜集 @OnClick 注解信息
        if (!EmptyUtils.isEmpty(onClickElements)) {
            for (Element element : onClickElements) {
                messager.printMessage(Diagnostic.Kind.NOTE, "@OnClick >>> " + element.getSimpleName());
                // 注解是否作用在方法上
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement executableElement = (ExecutableElement) element;
                    // 获取父节点，作为 tempOnClickMap 的 key
                    TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
                    // 包含key为 typeElement 的集合则直接添加
                    if (tempOnClickMap.containsKey(typeElement)) {
                        tempOnClickMap.get(typeElement).add(executableElement);
                    } else {
                        List<ExecutableElement> fields = new ArrayList();
                        fields.add(executableElement);
                        tempOnClickMap.put(typeElement, fields);
                    }
                }
            }
        }
    }

    /**
     * 根据注解生成类文件
     */
    private void createJavaFile() throws Exception {
        if (!EmptyUtils.isEmpty(tempBindViewMap)) {
            // 获取接口类型
            TypeElement viewBinderType = elementUtils.getTypeElement(Constants.VIEWBINDER);
            TypeElement clickListenerType = elementUtils.getTypeElement(Constants.CLICKLISTENER);
            TypeElement viewType = elementUtils.getTypeElement(Constants.VIEW);

            for (Map.Entry<TypeElement, List<VariableElement>> entry : tempBindViewMap.entrySet()) {
                // 获取 key , 也就是类名
                TypeElement typeElement = entry.getKey();
                ClassName className = ClassName.get(typeElement);

                // 实现接口泛型 implements ViewBinder
                ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get(viewBinderType), className);

                // 方法参数
                ParameterSpec parameterSpec = ParameterSpec.builder(className, Constants.TARGET_PARAMETER_NAME) // MainActivity
                        .addModifiers(Modifier.FINAL)
                        .build();

                // 实现方法体 public void bind(Activity activity)
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.BIND_METHOD_NAME) // bind 方法
                        .addAnnotation(Override.class) // 接口重写方法
                        .addModifiers(Modifier.PUBLIC) // 接口修饰符
                        .addParameter(parameterSpec); // 方法参数


                // 添加获取view方法内容
                for (VariableElement element : entry.getValue()) {
                    // 属性名
                    String fieldName = element.getSimpleName().toString();
                    // 属性值
                    int value = element.getAnnotation(BindView.class).value();

                    // target.tv = target.findViewById(R.id.tv)
                    String methodContent = "$N." + fieldName + " = $N.findViewById($L)";
                    // 添加方法内容
                    methodBuilder.addStatement(methodContent,
                            Constants.TARGET_PARAMETER_NAME,
                            Constants.TARGET_PARAMETER_NAME,
                            value);
                }

                // 添加view点击事件方法内容
                if (!EmptyUtils.isEmpty(tempOnClickMap)) {
                    for (Map.Entry<TypeElement, List<ExecutableElement>> methodEntry : tempOnClickMap.entrySet()) {
                        // 如果在同一个类里面
                        if (className.equals(ClassName.get(methodEntry.getKey()))) {
                            for (ExecutableElement element : methodEntry.getValue()) {
                                // 方法名
                                String methodName = element.getSimpleName().toString();
                                // 获取@Onclick 注解的值
                                int value = element.getAnnotation(OnClick.class).value();

                                messager.printMessage(Diagnostic.Kind.NOTE, "methodName >> " + methodName + ", value= " + value);

                                /**
                                 * target.findViewById(R.id.gone)
                                 *                 .setOnClickListener(new DebouncingOnClickListener() {
                                 *                     @Override
                                 *                     public void doClick(View v) {
                                 *                          target.click(v)
                                 *                     }
                                 *                 });
                                 */
                                StringBuilder builder = new StringBuilder();
                                builder.append("$N.");
                                builder.append("findViewById($L)");
                                builder.append(".setOnClickListener(new $T()");

                                // 添加方法内容
                                methodBuilder
                                        .beginControlFlow(builder.toString(),
                                                Constants.TARGET_PARAMETER_NAME,
                                                value,
                                                ClassName.get(clickListenerType))
                                        .beginControlFlow("public void doClick($T v)", ClassName.get(viewType))
                                        .addStatement("$N." + methodName + "(v)", Constants.TARGET_PARAMETER_NAME)
                                        .endControlFlow()
                                        .endControlFlow(")")
                                        .build();
                            }
                        }
                    }
                }

                // 生成必须是同包
                JavaFile.builder(className.packageName(),
                        TypeSpec.classBuilder(className.simpleName() + "$ViewBinder") // 类名
                                .addSuperinterface(typeName)// 实现 ViewBinder 接口
                                .addModifiers(Modifier.PUBLIC) // 类修饰
                                .addMethod(methodBuilder.build()) // 方法体
                                .build()) // 类构建完成
                        .build() // JavaFile 构建完成
                        .writeTo(filter); // 文件生成器开始生成类文件
            }
        }
    }

    /**
     * 设置支持的注解类型
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportAnnotations = new HashSet<>();
        supportAnnotations.add(Constants.BINDVIEW_ANNOTATION_TYPE);
        supportAnnotations.add(Constants.ONCLICK_ANNOTATION_TYPE);
        return supportAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
