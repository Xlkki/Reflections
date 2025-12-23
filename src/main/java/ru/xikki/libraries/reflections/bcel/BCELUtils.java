package ru.xikki.libraries.reflections.bcel;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.*;
import ru.xikki.libraries.reflections.ReflectionUtils;
import ru.xikki.libraries.reflections.condition.FieldCondition;
import ru.xikki.libraries.reflections.condition.MethodCondition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;

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


}
