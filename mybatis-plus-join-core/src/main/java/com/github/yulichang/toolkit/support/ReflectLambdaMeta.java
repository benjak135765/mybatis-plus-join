/*
 * Copyright (c) 2011-2022, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yulichang.toolkit.support;


import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.github.yulichang.toolkit.ClassUtils;
import com.github.yulichang.toolkit.ReflectionKit;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;

/**
 * Created by hcl at 2021/5/14
 */
public class ReflectLambdaMeta implements LambdaMeta {
    private static final Field FIELD_CAPTURING_CLASS;

    static {
        Field fieldCapturingClass;
        try {
            Class<SerializedLambda> aClass = SerializedLambda.class;
            fieldCapturingClass = ReflectionKit.setAccessible(aClass.getDeclaredField("capturingClass"));
        } catch (Throwable e) {
            // 解决高版本 jdk 的问题 gitee: https://gitee.com/baomidou/mybatis-plus/issues/I4A7I5
            fieldCapturingClass = null;
        }
        FIELD_CAPTURING_CLASS = fieldCapturingClass;
    }

    private final SerializedLambda lambda;

    public ReflectLambdaMeta(SerializedLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(StringPool.SEMICOLON)).replace(StringPool.SLASH, StringPool.DOT);
        return ClassUtils.toClassConfident(instantiatedType, getCapturingClassClassLoader());
    }

    private ClassLoader getCapturingClassClassLoader() {
        // 如果反射失败，使用默认的 classloader
        if (FIELD_CAPTURING_CLASS == null) {
            return null;
        }
        try {
            return ((Class<?>) FIELD_CAPTURING_CLASS.get(lambda)).getClassLoader();
        } catch (IllegalAccessException e) {
            throw new MybatisPlusException(e);
        }
    }

}
