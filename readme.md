# Reflections

Библиотека для работы с рефлексией и сканером классов в Java

## Dependencies

- BCEL (версия `6.11.0` или выше)
- ASM (версия `9.9.1` или выше)

## Import
### Gradle:
```gradle
repositories {
	//...
	maven {
		name = 'xikki-repo'
		url = 'https://repo.xikki.su/libraries'
	}
}

dependencies {
	//...
	implementation 'ru.xikki.libraries:Reflections:1.1'
}
```

### Maven
```xml
<project>
	<repositories>
		<!-- ... -->
		<repository>
			<id>xikki-repo</id>
			<url>https://repo.xikki.su/libraries</url>
		</repository>
	</repositories>

	<dependencies>
		<!-- ... -->
		<dependency>
			<groupId>ru.xikki.libraries</groupId>
			<artifactId>Reflections</artifactId>
			<version>1.1</version>
		</dependency>
	</dependencies>

</project>
```

## ReflectionUtils

`ReflectionUtils` - это класс с различным набором утилитарных методов
для работы с рефлексией:
- Инициализация класса (для определения static полей класса и выполнения блока кода static)
- Получение/Запись значения поля (в т.ч. защищенного поля, поля record-класса, final-static поля и прочих)
- Снятие защиты с внутренних классов (для доступа к полям, методам и конструкторам)
- Получение полей класса (в т.ч. унаследованных, синтетических, полей record-классов и прочих)
- Получение методов класса (в т.ч. унаследованных, нативных и прочих)
- Получение списка загруженных конкретным ClassLoader'ом классов
- Получение исходного файла/директории конкретного класса
- Получение класса, что вызывает метод
- Создание и встраивание новых значений enum классов

## IClassScanner

`IClassScanner` - это интерфейс, который используется для сканирования классов
без их загрузки в Runtime. В библиотеку добавлена реализация `SimpleClassScanner`, но
при желании Вы можете сделать свою реализацию интерфейса и заменить стандартную фабрику сканера
при помощи класса `ClassScannerProvider`.

Создать сканер классов можно несколькими способами:
1) Из файла или директории:
```Java
Path sourcePath = ...;

IClassScanner scanner = IClassScanner.of(sourcePath);
```
2) Из класса:
```Java
Class<?> clazz = ...;

IClassScanner scanner = IClassScanner.of(clazz);
```
**ВАЖНО:** В таком случае возьмется исходный файл/директория переданного класса

3) Из текущего класса:
```Java
IClassScanner scanner = IClassScanner.of(); 
```
**ВАЖНО:** В таком случае возьмется исходный файл/директория класса, в котором
вызывается этот метод

## IClassScannerContext
`IClassScannerContext` - это интерфейс, который используется для хранения
различных объектов. Контекст позволяет сканеру классов создавать
объекты или вызывать различные методы. В библиотеку добавлена реализация `SimpleClassScannerContext`, но
при желании Вы можете сделать свою реализацию интерфейса и заменить стандартную фабрику контекста
при помощи класса `ClassScannerContextProvider`.

Создать пустой контекст можно следующим образом:
```Java
IClassScannerContext context = IClassScannerContext.create();
```
В контекст можно добавлять различные объекты следующим образом:
```Java
Object entity = ...;
IClassScannerContext context = ...;

context.withEntity("entity_name", entity);
```
Объекты используются сканером классов для создания объектов и вызова различных методов следующим образом:
```Java
public class MyClass {
	
	private final int a;
	private final String b;
	
	public MyClass(int a, String b) {
		this.a = a;
		this.b = b;
	}
	
}
```
```Java
IClassScannerContext context = ...;
context.withEntity("a", 5)
	.withEntity("b", "abc");

MyClass object = context.createInstance(MyClass.class);
```
**ВАЖНО:** В случае отсутствия аргумента с конкретным именем, из контекста
возьмется объект, подходящий по типу, но с другим именем.

### IClassProcessor
`IClassProcessor` - интерфейс, который позволяет реализовывать процессор 
классов. Процессор классов позволяет Вам обрабатывать классы из сканера по условиям
(вроде имени класса, наличия аннотаций у методов или полей и прочего). В библиотеке 
имеются 3 абстракции для Ваших процессоров:
- `AbstractClassAnnotationProcessor` - процессор, который будет обрабатывать
классы с Вашей аннотацией (передается в конструкторе)
- `AbstractFieldAnnotationProcessor` - процессор, который будет обрабатывать
классы, поля которых имеют Вашу аннотацию (передается в конструкторе)
- `AbstractMethodAnnotationProcessor` - процессор, который будет обрабатывать
классы, методы которых имеют Вашу аннотацию (передается в конструкторе)

Пример процессора:
```Java
@ClassProcessor
final class PlaceholderMethodClassProcessor extends AbstractMethodAnnotationProcessor {

	private final IResolverManager resolverManager;
	private final IPlaceholderManager placeholderManager;
	private final Object holder;

	PlaceholderMethodClassProcessor(Predicate<String> classNamePredicator, @NonNull IResolverManager resolverManager, @NonNull IPlaceholderManager placeholderManager, Object holder) {
		super(classNamePredicator, IgnorePlaceholders.class, Placeholder.class);
		this.resolverManager = resolverManager;
		this.holder = holder;
		this.placeholderManager = placeholderManager;
	}
	
	@Override
	public boolean shouldLoadClass(@NonNull Holder holder, @NonNull JavaClass javaClass) {
		return true;
	}
	
	@Override
	public boolean shouldCreateInstance(@NonNull Holder holder, @NonNull JavaClass javaClass) {
		return BCELUtils.hasAnnotatedNonStaticField(javaClass, this.methodAnnotationSignature);
	}

	@Override
	public void processInstance(@NonNull Holder holder, @NonNull Object instance) {
		this.placeholderManager.registerPlaceholdersFrom(this.resolverManager, instance, instance.getClass(), this.holder);
	}

	@Override
	public void processClass(@NonNull Holder holder, @NonNull Class<?> clazz) {
		this.placeholderManager.registerPlaceholdersFrom(this.resolverManager, null, clazz, this.holder);
	}
	
	@Override
	public void processClass(@NonNull Holder holder, @NonNull JavaClass javaClass) {
		
	}

}
```
Данный процессор ищет все классы, методы которых содержат аннотацию `@Placeholder`, и регистрирует эти методы, как
плейсхолдеры. При этом, процессор игнорирует классы, помеченные аннотацией `@IgnorePlaceholders`. 
Аннотация `@ClassProcessor` позволяет сканеру классов самому найти и зарегистрировать этот процессор.
Все параметры конструктора берутся из IClassScannerContext.

Зарегистрировать процессор можно двумя способами:
1) Вручную:
```Java
IClassScanner scanner = ...;
IClassProcessor processor = ...;

scanner.registerProcessor(processor);
```
2) При помощи аннотации `@ClassProcessor`:
```Java
@ClassProcessor(ProcessorOrder.NORMAL)
final class MyProcessor implements IClassProcessor {
	
	/**
	 * ...
	 * */
	
}
```
```Java
IClassScanner scanner = ...;
IClassScannerContext context = ...;
ClassLoader loader = ...;

List<IClassProcessor> processors = scanner.findProcessors(context, ProcessorOrder.NORMAL, loader);
scanner.registerProcessors(processors);
```
**ВАЖНО:** Не забудьте добавить в контекст все необходимые параметры для создания
процессоров через конструктор


Запустить процессоры Вы можете сделующим образом:
```Java
IClassScanner scanner = ...;
IClassScannerContext context = ...;
ClassLoader loader = ...;

scanner.processClasses(context, loader);
```

### IClassModifier
`IClassModifier` - интерфейс, который позволяет реализовывать модификаторы
классов. Модификатор класса позволяет Вам изменять классы на этапе запуска
программы. Для модификации используется `BCEL`. Аналогично процессорам, в
библиотеке содержатся 3 абстракции для Ваших процессоров:
- `AbstractClassAnnotationModifier` - модификатор, который будет обрабатывать
  классы с Вашей аннотацией (передается в конструкторе)
- `AbstractFieldAnnotationModifier` - модификатор, который будет обрабатывать
  классы, поля которых имеют Вашу аннотацию (передается в конструкторе)
- `AbstractMethodAnnotationModifier` - модификатор, который будет обрабатывать
  классы, методы которых имеют Вашу аннотацию (передается в конструкторе)

Пример модификатора:
```Java
@ClassModifier
final class CacheClearClassModifier extends AbstractMethodAnnotationModifier {

	CacheClearClassModifier(Predicate<String> classNameFilter) {
		super(classNameFilter, null, CacheClear.class);
	}

	@Override
	public void modifyClass(@NonNull Holder holder, @NonNull ClassGen classGenerator) {
		ConstantPoolGen poolGenerator = classGenerator.getConstantPool();
		for (Method originalMethod : classGenerator.getMethods()) {
			AnnotationEntry cacheClearEntry = BCELUtils.getAnnotation(originalMethod, this.methodAnnotationSignature);
			if (cacheClearEntry == null) {
				continue;
			}
			String cacheId = BCELUtils.getOptionalFieldValue(cacheClearEntry, "value")
					.orElse("");
			String fieldName = "CACHE$" + cacheId;
			CacheModifierUtils.createCacheFieldSafely(classGenerator, fieldName);
			MethodGen methodGenerator = new MethodGen(originalMethod, classGenerator.getClassName(), poolGenerator);
			InstructionList oldInstructions = methodGenerator.getInstructionList();
			InstructionFactory factory = new InstructionFactory(classGenerator);
			InstructionList endInstructions = new InstructionList();

			endInstructions.append(
					factory.createGetStatic(
							classGenerator.getClassName(),
							fieldName,
							Type.getType(ICache.class)
					)
			);
			endInstructions.append(
					factory.createInvoke(
							ICache.class.getName(),
							"clear",
							Type.VOID,
							new Type[]{},
							Constants.INVOKEINTERFACE
					)
			);

			BCELUtils.injectInstructionsToEnd(oldInstructions, endInstructions);

			oldInstructions.setPositions();
			methodGenerator.setInstructionList(oldInstructions);
			methodGenerator.setMaxLocals();
			methodGenerator.setMaxStack();
			classGenerator.replaceMethod(originalMethod, methodGenerator.getMethod());
		}
	}

}
```
Данный процессор ищет все классы, методы которых содержат аннотацию `@CacheClear`, и
переписывает эти методы таким образом, чтобы после выполнения оригинального метода,
кеш с айди, переданным в аннотации, очистился. 

Зарегистрировать модификаторы можно двумя способами:
1) Вручную:
```Java
IClassScanner scanner = ...;
IClassModifier modifier = ...;

scanner.registerModifier(modifier);
```
2) При помощи аннотации `@ClassModifier`:
```Java
@ClassModifier
final class MyModifier implements IClassModifier {
	
	/**
	 * ...
	 * */
	
}
```
```Java
IClassScanner scanner = ...;
IClassScannerContext context = ...;
ClassLoader loader = ...;

List<IClassModifier> modifiers = scanner.findModifiers(context, loader);
scanner.registerModifiers(modifiers);
```
**ВАЖНО:** Не забудьте добавить в контекст все необходимые параметры для создания
модификаторов через конструктор

Модификация классов выполняется в 2 этапа:
1) Изменение классов (без их загрузки в Runtime):
```Java
IClassScanner scanner = ...;

scanner.modifyClasses();
```
2) Загрузка измененных классов в Runtime:
```Java
IClassScanner scanner = ...;
ClassLoader loader = ...;

scanner.dump(loader);
```
**ВАЖНО:** Данные действия необходимо выполнять до загрузки изменяемых классов
в Runtime. Рекомендуется делать это на этапе запуска приложения (модуля, плагина и пр.)

На самом деле, библиотека содержит еще парочку вещей, но мне, 
откровенно говоря, лень их расписывать
