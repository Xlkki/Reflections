package ru.xikki.libraries.reflections.bcel;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import ru.xikki.libraries.reflections.ReflectionUtils;
import ru.xikki.libraries.reflections.condition.FieldCondition;
import ru.xikki.libraries.reflections.condition.MethodCondition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

@UtilityClass
public final class BCELUtils {

	/**
	 * Get java class from BCEL type in specified class loader
	 *
	 * @param loader Class loader from which class will be loaded
	 * @param type   BCEL class type
	 * @return Java class from BCEL type
	 */
	@NonNull
	@SneakyThrows
	public Class<?> getClass(@NonNull ClassLoader loader, @NonNull Type type) {
		if (type instanceof ArrayType arrayType) {
			return Array.newInstance(BCELUtils.getClass(loader, arrayType.getElementType()), 0).getClass();
		}
		if (type instanceof BasicType basicType) {
			String typeSignature = basicType.getSignature();
			return switch (typeSignature.charAt(0)) {
				case 'Z' -> boolean.class;
				case 'B' -> byte.class;
				case 'C' -> char.class;
				case 'S' -> short.class;
				case 'I' -> int.class;
				case 'J' -> long.class;
				case 'F' -> float.class;
				case 'D' -> double.class;
				case 'V' -> void.class;
				default -> throw new IllegalArgumentException("Unknown basic type signature " + typeSignature);
			};
		}
		if (type instanceof ObjectType objectType) {
			return loader.loadClass(objectType.getClassName());
		}
		throw new IllegalArgumentException("Can not get class from type " + type);
	}

	/**
	 * Check if instruction list has class init instruction calling
	 *
	 * @param instructions  Instruction list
	 * @param poolGenerator Class from which instructions were obtained pool generator
	 * @param className     Class name init instructions of which should be checked
	 * @return True - if init instruction is founded, otherwise - false
	 */
	public boolean hasClassInitCalling(@NonNull InstructionList instructions, @NonNull ConstantPoolGen poolGenerator, @NonNull String className) {
		return Arrays.stream(instructions.getInstructions())
				.filter((instruction) -> instruction instanceof InvokeInstruction)
				.map((instruction) -> ((InvokeInstruction) instruction))
				.filter((instruction) -> instruction.getMethodName(poolGenerator).equals("<init>"))
				.anyMatch((instruction) -> instruction.getClassName(poolGenerator).equals(className));
	}

	/**
	 * Check if instruction list has class init instruction calling
	 *
	 * @param instructions  Instruction list
	 * @param poolGenerator Class from which instructions were obtained pool generator
	 * @param clazz         Class init instructions of which should be checked
	 * @return True - if init instruction is founded, otherwise - false
	 */
	public boolean hasClassInitCalling(@NonNull InstructionList instructions, @NonNull ConstantPoolGen poolGenerator, @NonNull Class<?> clazz) {
		return BCELUtils.hasClassInitCalling(instructions, poolGenerator, clazz.getName());
	}

	/**
	 * Check if instruction list has class init instruction calling
	 *
	 * @param instructions   Instruction list
	 * @param poolGenerator  Class from which instructions were obtained pool generator
	 * @param classGenerator Class generator	 init instructions of which should be checked
	 * @return True - if init instruction is founded, otherwise - false
	 */
	public boolean hasClassInitCalling(@NonNull InstructionList instructions, @NonNull ConstantPoolGen poolGenerator, @NonNull ClassGen classGenerator) {
		return BCELUtils.hasClassInitCalling(instructions, poolGenerator, classGenerator.getClassName());
	}

	/**
	 * Check if instruction list has class init instruction calling
	 *
	 * @param instructions  Instruction list
	 * @param poolGenerator Class from which instructions were obtained pool generator
	 * @param javaClass     BCEL class init instructions of which should be checked
	 * @return True - if init instruction is founded, otherwise - false
	 */
	public boolean hasClassInitCalling(@NonNull InstructionList instructions, @NonNull ConstantPoolGen poolGenerator, @NonNull JavaClass javaClass) {
		return BCELUtils.hasClassInitCalling(instructions, poolGenerator, javaClass.getClassName());
	}

	/**
	 * Check if method has class init instruction calling
	 *
	 * @param methodGenerator Method generator in which init calling should be checked
	 * @param poolGenerator   Class from which instructions were obtained pool generator
	 * @param className       Class name init instructions of which should be checked
	 * @return True - if init instruction is founded, otherwise - false
	 */
	public boolean hasClassInitCalling(@NonNull MethodGen methodGenerator, @NonNull ConstantPoolGen poolGenerator, @NonNull String className) {
		return BCELUtils.hasClassInitCalling(methodGenerator.getInstructionList(), poolGenerator, className);
	}

	/**
	 * Check if method has class init instruction calling
	 *
	 * @param methodGenerator Method generator in which init calling should be checked
	 * @param poolGenerator   Class from which instructions were obtained pool generator
	 * @param clazz           Class init instructions of which should be checked
	 * @return True - if init instruction is founded, otherwise - false
	 */
	public boolean hasClassInitCalling(@NonNull MethodGen methodGenerator, @NonNull ConstantPoolGen poolGenerator, @NonNull Class<?> clazz) {
		return BCELUtils.hasClassInitCalling(methodGenerator, poolGenerator, clazz.getName());
	}

	/**
	 * Check if method has class init instruction calling
	 *
	 * @param methodGenerator Method generator in which init calling should be checked
	 * @param poolGenerator   Class from which instructions were obtained pool generator
	 * @param classGenerator  Class generator	 init instructions of which should be checked
	 * @return True - if init instruction is founded, otherwise - false
	 */
	public boolean hasClassInitCalling(@NonNull MethodGen methodGenerator, @NonNull ConstantPoolGen poolGenerator, @NonNull ClassGen classGenerator) {
		return BCELUtils.hasClassInitCalling(methodGenerator, poolGenerator, classGenerator.getClassName());
	}

	/**
	 * Check if method has class init instruction calling
	 *
	 * @param methodGenerator Method generator in which init calling should be checked
	 * @param poolGenerator   Class from which instructions were obtained pool generator
	 * @param javaClass       BCEL class init instructions of which should be checked
	 * @return True - if init instruction is founded, otherwise - false
	 */
	public boolean hasClassInitCalling(@NonNull MethodGen methodGenerator, @NonNull ConstantPoolGen poolGenerator, @NonNull JavaClass javaClass) {
		return BCELUtils.hasClassInitCalling(methodGenerator, poolGenerator, javaClass.getClassName());
	}


	/**
	 * Get java class from BCEL class
	 *
	 * @param loader    Class loader from which class will be loaded
	 * @param javaClass BCEL class
	 * @return Java class from BCEL class
	 */
	@NonNull
	@SneakyThrows
	public Class<?> getClass(@NonNull ClassLoader loader, @NonNull JavaClass javaClass) {
		return loader.loadClass(javaClass.getClassName());
	}

	/**
	 * Get java method from BCEL method
	 *
	 * @param clazz  Class from which method will be loaded
	 * @param method BCEL method
	 * @return Java method from BCEL method
	 */
	@NonNull
	public java.lang.reflect.Method getMethod(@NonNull Class<?> clazz, @NonNull Method method) {
		return ReflectionUtils.getMethodOrThrow(
				clazz,
				MethodCondition.create()
						.withName(method.getName())
						.withStatic(method.isStatic())
						.withReturnType(BCELUtils.getClass(clazz.getClassLoader(), method.getReturnType()))
						.withParameters(
								Arrays.stream(method.getArgumentTypes())
										.map((type) -> BCELUtils.getClass(clazz.getClassLoader(), type))
										.toArray(Class[]::new)
						)
		);
	}

	/**
	 * Get java field from BCEL field
	 *
	 * @param clazz Class from which field will be loaded
	 * @param field BCEL field
	 * @return Java field from BCEL field
	 */
	@NonNull
	public java.lang.reflect.Field getField(@NonNull Class<?> clazz, @NonNull Field field) {
		return ReflectionUtils.getFieldOrThrow(
				clazz,
				FieldCondition.create()
						.withName(field.getName())
						.withStatic(field.isStatic())
						.withType(BCELUtils.getClass(clazz.getClassLoader(), field.getType()))
		);
	}

	/**
	 * Get annotation signature from annotation class
	 *
	 * @param annotationClass Annotation class
	 * @return Java annotation signature as string <code>L[class_name];</code>
	 */
	@NonNull
	public String getAnnotationSignature(@NonNull Class<? extends Annotation> annotationClass) {
		return "L" + Utility.packageToPath(annotationClass.getName()) + ";";
	}

	/**
	 * Get BCEL annotation entry from BCEL class by annotation signature
	 *
	 * @param javaClass           BCEL class
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return BCEL annotation entry
	 */
	public AnnotationEntry getAnnotation(@NonNull JavaClass javaClass, @NonNull String annotationSignature) {
		return Arrays.stream(javaClass.getAnnotationEntries())
				.filter((entry) -> entry.getAnnotationType().equals(annotationSignature))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Get BCEL annotation entry from BCEL class by BCEL type
	 *
	 * @param javaClass BCEL class
	 * @param type      BCEL type
	 * @return BCEL annotation entry
	 */
	public AnnotationEntry getAnnotation(@NonNull JavaClass javaClass, @NonNull ObjectType type) {
		return BCELUtils.getAnnotation(javaClass, type.getSignature());
	}

	/**
	 * Get BCEL annotation entry from BCEL class by java annotation class
	 *
	 * @param javaClass       BCEL class
	 * @param annotationClass Java annotation class
	 * @return BCEL annotation entry
	 */
	public AnnotationEntry getAnnotation(@NonNull JavaClass javaClass, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.getAnnotation(javaClass, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Get BCEL annotation entry from BCEL method by annotation signature
	 *
	 * @param method              BCEL method
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return BCEL annotation entry
	 */
	public AnnotationEntry getAnnotation(@NonNull Method method, @NonNull String annotationSignature) {
		return Arrays.stream(method.getAnnotationEntries())
				.filter((entry) -> entry.getAnnotationType().equals(annotationSignature))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Get BCEL annotation entry from BCEL method by BCEL type
	 *
	 * @param method BCEL method
	 * @param type   BCEL type
	 * @return BCEL annotation entry
	 */
	public AnnotationEntry getAnnotation(@NonNull Method method, @NonNull ObjectType type) {
		return BCELUtils.getAnnotation(method, type.getSignature());
	}

	/**
	 * Get BCEL annotation entry from BCEL method by java annotation class
	 *
	 * @param method          BCEL method
	 * @param annotationClass Java annotation class
	 * @return BCEL annotation entry
	 */
	public AnnotationEntry getAnnotation(@NonNull Method method, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.getAnnotation(method, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Get BCEL annotation entry from BCEL field by annotation signature
	 *
	 * @param field               BCEL field
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return BCEL annotation entry
	 */
	public AnnotationEntry getAnnotation(@NonNull Field field, @NonNull String annotationSignature) {
		return Arrays.stream(field.getAnnotationEntries())
				.filter((entry) -> entry.getAnnotationType().equals(annotationSignature))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Get BCEL annotation entry from BCEL field by BCEL type
	 *
	 * @param field BCEL field
	 * @param type  BCEL type
	 * @return BCEL annotation entry
	 */
	public AnnotationEntry getAnnotation(@NonNull Field field, @NonNull ObjectType type) {
		return BCELUtils.getAnnotation(field, type.getSignature());
	}

	/**
	 * Get BCEL annotation entry from BCEL field by java annotation class
	 *
	 * @param field           BCEL field
	 * @param annotationClass Java annotation class
	 * @return BCEL annotation entry
	 */
	public AnnotationEntry getAnnotation(@NonNull Field field, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.getAnnotation(field, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Get BCEL annotation entry field value by field name
	 *
	 * @param entry BCEL annotation entry
	 * @param name  field name
	 * @return optional with field value (in string format)
	 */
	public Optional<String> getOptionalFieldValue(@NonNull AnnotationEntry entry, @NonNull String name) {
		return Arrays.stream(entry.getElementValuePairs())
				.filter((pair) -> pair.getNameString().equals(name))
				.findFirst()
				.map(ElementValuePair::getValue)
				.map(ElementValue::stringifyValue);
	}

	/**
	 * Get BCEL annotation entry field value by field name
	 *
	 * @param entry BCEL annotation entry
	 * @param name  field name
	 * @return field value (in string)
	 */
	public String getFieldValue(@NonNull AnnotationEntry entry, @NonNull String name) {
		return BCELUtils.getOptionalFieldValue(entry, name)
				.orElse(null);
	}

	/**
	 * Check if BCEL class has annotation by signature
	 *
	 * @param javaClass           BCEL class
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return true if class has annotation, otherwise false
	 */
	public boolean hasAnnotation(@NonNull JavaClass javaClass, @NonNull String annotationSignature) {
		return Arrays.stream(javaClass.getAnnotationEntries())
				.map(AnnotationEntry::getAnnotationType)
				.anyMatch(Predicate.isEqual(annotationSignature));
	}

	/**
	 * Check if BCEL class has annotation by BCEL type
	 *
	 * @param javaClass BCEL class
	 * @param type      BCEL type
	 * @return true if class has annotation, otherwise false
	 */
	public boolean hasAnnotation(@NonNull JavaClass javaClass, @NonNull ObjectType type) {
		return BCELUtils.hasAnnotation(javaClass, type.getSignature());
	}

	/**
	 * Check if BCEL class has annotation by java annotation class
	 *
	 * @param javaClass       BCEL class
	 * @param annotationClass Java annotation class
	 * @return true if class has annotation, otherwise false
	 */
	public boolean hasAnnotation(@NonNull JavaClass javaClass, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.hasAnnotation(javaClass, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Check if BCEL method has annotation by annotation signature
	 *
	 * @param method              BCEL method
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return true if method has annotation, otherwise false
	 */
	public boolean hasAnnotation(@NonNull Method method, @NonNull String annotationSignature) {
		return Arrays.stream(method.getAnnotationEntries())
				.map(AnnotationEntry::getAnnotationType)
				.anyMatch(Predicate.isEqual(annotationSignature));
	}

	/**
	 * Check if BCEL method has annotation by BCEL type
	 *
	 * @param method BCEL method
	 * @param type   BCEL type
	 * @return true if method has annotation, otherwise false
	 */
	public boolean hasAnnotation(@NonNull Method method, @NonNull ObjectType type) {
		return BCELUtils.hasAnnotation(method, type.getSignature());
	}

	/**
	 * Check if BCEL method has annotation by java annotation class
	 *
	 * @param method          BCEL method
	 * @param annotationClass Java annotation class
	 * @return true if method has annotation, otherwise false
	 */
	public boolean hasAnnotation(@NonNull Method method, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.hasAnnotation(method, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Check if BCEL field has annotation by annotation signature
	 *
	 * @param field               BCEL field
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return true if field has annotation, otherwise false
	 */
	public boolean hasAnnotation(@NonNull org.apache.bcel.classfile.Field field, @NonNull String annotationSignature) {
		return Arrays.stream(field.getAnnotationEntries())
				.map(AnnotationEntry::getAnnotationType)
				.anyMatch(Predicate.isEqual(annotationSignature));
	}

	/**
	 * Check if BCEL field has annotation by BCEL type
	 *
	 * @param field BCEL field
	 * @param type  BCEL type
	 * @return true if field has annotation, otherwise false
	 */
	public boolean hasAnnotation(@NonNull org.apache.bcel.classfile.Field field, @NonNull ObjectType type) {
		return BCELUtils.hasAnnotation(field, type.getSignature());
	}

	/**
	 * Check if BCEL field has annotation by java annotation class
	 *
	 * @param field           BCEL field
	 * @param annotationClass Java annotation class
	 * @return true if method has annotation, otherwise false
	 */
	public boolean hasAnnotation(@NonNull org.apache.bcel.classfile.Field field, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.hasAnnotation(field, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Check if BCEL class has method with specified annotation by annotation signature
	 *
	 * @param javaClass           BCEL class
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return true if class has method with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedMethod(@NonNull JavaClass javaClass, @NonNull String annotationSignature) {
		return Arrays.stream(javaClass.getMethods())
				.anyMatch((method) -> BCELUtils.hasAnnotation(method, annotationSignature));
	}

	/**
	 * Check if BCEL class has method with specified annotation by BCEL type
	 *
	 * @param javaClass BCEL class
	 * @param type      BCEL type
	 * @return true if class has method with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedMethod(@NonNull JavaClass javaClass, @NonNull ObjectType type) {
		return BCELUtils.hasAnnotatedMethod(javaClass, type.getSignature());
	}

	/**
	 * Check if BCEL class has method with specified annotation by java annotation class
	 *
	 * @param javaClass       BCEL class
	 * @param annotationClass Java annotation class
	 * @return true if class has method with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedMethod(@NonNull JavaClass javaClass, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.hasAnnotatedMethod(javaClass, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Check if BCEL class has method with specified annotation by annotation signature
	 *
	 * @param javaClass           BCEL class
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return true if class has method with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedStaticMethod(@NonNull JavaClass javaClass, @NonNull String annotationSignature) {
		return Arrays.stream(javaClass.getMethods())
				.filter(Method::isStatic)
				.anyMatch((method) -> BCELUtils.hasAnnotation(method, annotationSignature));
	}

	/**
	 * Check if BCEL class has method with specified annotation by BCEL type
	 *
	 * @param javaClass BCEL class
	 * @param type      BCEL type
	 * @return true if class has method with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedStaticMethod(@NonNull JavaClass javaClass, @NonNull ObjectType type) {
		return BCELUtils.hasAnnotatedStaticMethod(javaClass, type.getSignature());
	}

	/**
	 * Check if BCEL class has method with specified annotation by java annotation class
	 *
	 * @param javaClass       BCEL class
	 * @param annotationClass Java annotation class
	 * @return true if class has method with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedStaticMethod(@NonNull JavaClass javaClass, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.hasAnnotatedStaticMethod(javaClass, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Check if BCEL class has non-method with specified annotation by annotation signature
	 *
	 * @param javaClass           BCEL class
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return true if class has non-method with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedNonStaticMethod(@NonNull JavaClass javaClass, @NonNull String annotationSignature) {
		return Arrays.stream(javaClass.getMethods())
				.filter(Predicate.not(Method::isStatic))
				.anyMatch((method) -> BCELUtils.hasAnnotation(method, annotationSignature));
	}

	/**
	 * Check if BCEL class has non-method with specified annotation by BCEL type
	 *
	 * @param javaClass BCEL class
	 * @param type      BCEL type
	 * @return true if class has non-method with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedNonStaticMethod(@NonNull JavaClass javaClass, @NonNull ObjectType type) {
		return BCELUtils.hasAnnotatedNonStaticMethod(javaClass, type.getSignature());
	}

	/**
	 * Check if BCEL class has non-method with specified annotation by java annotation class
	 *
	 * @param javaClass       BCEL class
	 * @param annotationClass Java annotation class
	 * @return true if class has non-method with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedNonStaticMethod(@NonNull JavaClass javaClass, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.hasAnnotatedNonStaticMethod(javaClass, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Check if BCEL class has field with specified annotation by annotation signature
	 *
	 * @param javaClass           BCEL class
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return true if class has field with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedField(@NonNull JavaClass javaClass, @NonNull String annotationSignature) {
		return Arrays.stream(javaClass.getFields())
				.anyMatch((field) -> BCELUtils.hasAnnotation(field, annotationSignature));
	}

	/**
	 * Check if BCEL class has field with specified annotation by BCEL type
	 *
	 * @param javaClass BCEL class
	 * @param type      BCEL type
	 * @return true if class has field with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedField(@NonNull JavaClass javaClass, @NonNull ObjectType type) {
		return BCELUtils.hasAnnotatedField(javaClass, type.getSignature());
	}

	/**
	 * Check if BCEL class has field with specified annotation by java annotation class
	 *
	 * @param javaClass       BCEL class
	 * @param annotationClass Java annotation class
	 * @return true if class has field with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedField(@NonNull JavaClass javaClass, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.hasAnnotatedField(javaClass, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Check if BCEL class has field with specified annotation by annotation signature
	 *
	 * @param javaClass           BCEL class
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return true if class has field with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedStaticField(@NonNull JavaClass javaClass, @NonNull String annotationSignature) {
		return Arrays.stream(javaClass.getFields())
				.filter(org.apache.bcel.classfile.Field::isStatic)
				.anyMatch((field) -> BCELUtils.hasAnnotation(field, annotationSignature));
	}

	/**
	 * Check if BCEL class has field with specified annotation by BCEL type
	 *
	 * @param javaClass BCEL class
	 * @param type      BCEL type
	 * @return true if class has field with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedStaticField(@NonNull JavaClass javaClass, @NonNull ObjectType type) {
		return BCELUtils.hasAnnotatedStaticField(javaClass, type.getSignature());
	}

	/**
	 * Check if BCEL class has field with specified annotation by java annotation class
	 *
	 * @param javaClass       BCEL class
	 * @param annotationClass Java annotation class
	 * @return true if class has field with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedStaticField(@NonNull JavaClass javaClass, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.hasAnnotatedStaticField(javaClass, BCELUtils.getAnnotationSignature(annotationClass));
	}

	/**
	 * Check if BCEL class has non-field with specified annotation by annotation signature
	 *
	 * @param javaClass           BCEL class
	 * @param annotationSignature Annotation signature. For example <code>Ljava.lang.FunctionalInterface;</code>
	 * @return true if class has non-field with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedNonStaticField(@NonNull JavaClass javaClass, @NonNull String annotationSignature) {
		return Arrays.stream(javaClass.getFields())
				.filter(Predicate.not(org.apache.bcel.classfile.Field::isStatic))
				.anyMatch((field) -> BCELUtils.hasAnnotation(field, annotationSignature));
	}

	/**
	 * Check if BCEL class has non-field with specified annotation by BCEL type
	 *
	 * @param javaClass BCEL class
	 * @param type      BCEL type
	 * @return true if class has non-field with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedNonStaticField(@NonNull JavaClass javaClass, @NonNull ObjectType type) {
		return BCELUtils.hasAnnotatedNonStaticField(javaClass, type.getSignature());
	}

	/**
	 * Check if BCEL class has non-field with specified annotation by java annotation class
	 *
	 * @param javaClass       BCEL class
	 * @param annotationClass Java annotation class
	 * @return true if class has non-field with specified annotation, otherwise false
	 */
	public boolean hasAnnotatedNonStaticField(@NonNull JavaClass javaClass, @NonNull Class<? extends Annotation> annotationClass) {
		return BCELUtils.hasAnnotatedNonStaticField(javaClass, BCELUtils.getAnnotationSignature(annotationClass));
	}


}
