/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * BeanPostProcessor是Spring容器的一个扩展点，可以进行自定义的实例化、初始化、依赖装配、依赖检查等
 * 流程，即可以覆盖默认的实例化，也可以增强初始化、依赖注入、依赖检等流程
 *
 * Factory hook that allows for custom modification of new bean instances &mdash;
 * for example, checking for marker interfaces or wrapping beans with proxies.
 *
 *  如可以检查相应的标识接口完成一些自定义功能实现，如包装目标对象到代理对象
 *
 * <p>Typically, post-processors that populate beans via marker interfaces
 * or the like will implement {@link #postProcessBeforeInitialization},
 * while post-processors that wrap beans with proxies will normally
 * implement {@link #postProcessAfterInitialization}.
 *
 *  通过标记接口填充bean的post-processors 或者类似的将实现  postProcessBeforeInitialization，
 *  而使用代理包装bean的后处理器通常实现 postProcessAfterInitialization
 *
 * <h3>Registration</h3>
 * <p>An {@code ApplicationContext} can autodetect {@code BeanPostProcessor} beans
 * in its bean definitions and apply those post-processors to any beans subsequently
 * created. A plain {@code BeanFactory} allows for programmatic registration of
 * post-processors, applying them to all beans created through the bean factory.
 *
 * ApplicationContext 可以在其bean定义中自动检测 BeanPostProcessor 并且随后将这些 post-processors 用于任何bean的创建
 * 普通的 BeanFactory 允许编程注册post-processors ，将它们应用于通过bean工厂创建的所有bean
 *
 * <h3>Ordering</h3>
 * <p>{@code BeanPostProcessor} beans that are autodetected in an
 * {@code ApplicationContext} will be ordered according to
 * {@link org.springframework.core.PriorityOrdered} and
 * {@link org.springframework.core.Ordered} semantics. In contrast,
 * {@code BeanPostProcessor} beans that are registered programmatically with a
 * {@code BeanFactory} will be applied in the order of registration; any ordering
 * semantics expressed through implementing the
 * {@code PriorityOrdered} or {@code Ordered} interface will be ignored for
 * programmatically registered post-processors. Furthermore, the
 * {@link org.springframework.core.annotation.Order @Order} annotation is not
 * taken into account for {@code BeanPostProcessor} beans.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 10.10.2003
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 *
 *
 * 类的初始化：是完成程序执行前的准备工作。在这个阶段，静态的（变量，方法，代码块）会被执行。同时在会开辟一块存储空间用来存放静态的数据。初始化只在类加载的时候执行一次。
 * 类的实例化：是指创建一个对象的过程。这个过程中会在堆中开辟内存，将一些非静态的方法，变量存放在里面。在程序执行的过程中，可以创建多个对象，既多次实例化。每次实例化都会开辟一块新的内存。
 *
 * 类的完整生命周期包括7个部分：加载——验证——准备——解析——初始化——使用——卸载，
 *
 * 2.2类加载过程：
 *    加载：通过类名获取类的二进制字节流是通过类加载器来完成的。其加载过程使用“双亲委派模型”
 *
 *  验证：当一个类被加载之后，必须要验证一下这个类是否合法，比如这个类是不是符合字节码的格式、变量与方法是不是有重复、数据类型是不是有效、继承与实现是否合乎标准等等。总之，这个阶段的目的就是保证加载的类是*
 *  能够被jvm所运行。
 *   准备：为类变量（静态变量）在方法区分配内存，并设置零值。注意：这里是类变量，不是实例变量，实例变量是对象分配到堆内存时根据运行时动态生成的。
 *
 *   解析：把常量池中的符号引用解析为直接引用：根据符号引用所作的描述，在内存中找到符合描述的目标并把目标指针指针返回。
 *
 *   初始化：类的初始化过程是这样的：按照顺序自上而下运行类中的变量赋值语句和静态语句，如果有父类，则首先按照顺序运行父类中的变量赋值语句和静态语句在类的初始化阶段，只会初始化与类相关的静态赋值语句和静态*
 *   语句，也就是有static关键字修饰的信息，而没有static修饰的赋值语句和执行语句在实例化对象的时候才会运行。执行<clinit>()方法（clinit是class initialize的简写）
 *
 *  实例化：在堆区分配内存空间，执行实例对象初始化，设置引用变量a指向刚分配的内存地址
 *
 *
 *  接口中两个方法不能返回null，如果返回null那么在后续初始化方法将报空指针异常或者通过getBean()方法获取不到bena实例对象 ，因为后置处理器从Spring IoC容器中取出bean实例对象没有再次放回IoC容器中
 */
public interface BeanPostProcessor {

	/**
   *
   * bean 初始化前前增强
   *	// 在Bean实例化/依赖注入完毕以及自定义的初始化方法之前调用。什么叫自定义初始化方法：比如init-method、比如@PostConstruct标、比如实现InitailztingBean接口的方法等等

   * Apply this {@code BeanPostProcessor} to the given new bean instance <i>before</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 */
	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
   *    * bean 初始后前前增强 同InitializingBean
   *
	 * Apply this {@code BeanPostProcessor} to the given new bean instance <i>after</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * <p>In case of a FactoryBean, this callback will be invoked for both the FactoryBean
	 * instance and the objects created by the FactoryBean (as of Spring 2.0). The
	 * post-processor can decide whether to apply to either the FactoryBean or created
	 * objects or both through corresponding {@code bean instanceof FactoryBean} checks.
	 * <p>This callback will also be invoked after a short-circuiting triggered by a
	 * {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} method,
	 * in contrast to all other {@code BeanPostProcessor} callbacks.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.FactoryBean
	 */
	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
