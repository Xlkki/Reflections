package ru.xikki.libraries.reflections.condition;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(staticName = "create")
public final class MethodCondition extends ClassMemberCondition<Method> {

	@NonNull
	@Override
	public MethodCondition withName(@NonNull String name) {
		return (MethodCondition) super.withName(name);
	}

	@NonNull
	@Override
	public MethodCondition withNative(boolean isNative) {
		return (MethodCondition) super.withNative(isNative);
	}

	@NonNull
	@Override
	public MethodCondition withStatic(boolean isStatic) {
		return (MethodCondition) super.withStatic(isStatic);
	}

	@NonNull
	@Override
	public MethodCondition withPublic(boolean isPublic) {
		return (MethodCondition) super.withPublic(isPublic);
	}

	@NonNull
	@Override
	public MethodCondition withPrivate(boolean isPrivate) {
		return (MethodCondition) super.withPrivate(isPrivate);
	}

	@NonNull
	@Override
	public MethodCondition withProtected(boolean isProtected) {
		return (MethodCondition) super.withProtected(isProtected);
	}

	@NonNull
	@Override
	public MethodCondition withAbstract(boolean isAbstract) {
		return (MethodCondition) super.withAbstract(isAbstract);
	}

	@NonNull
	@Override
	public MethodCondition withFinal(boolean isFinal) {
		return (MethodCondition) super.withFinal(isFinal);
	}

	@NonNull
	@Override
	public MethodCondition withInterface(boolean isInterface) {
		return (MethodCondition) super.withInterface(isInterface);
	}

	@NonNull
	@Override
	public MethodCondition withSynchronized(boolean isSynchronized) {
		return (MethodCondition) super.withSynchronized(isSynchronized);
	}

	@NonNull
	@Override
	public MethodCondition withStrict(boolean isStrict) {
		return (MethodCondition) super.withStrict(isStrict);
	}

	@NonNull
	@Override
	public MethodCondition withDeclaredClass(@NonNull Class<?> clazz) {
		return (MethodCondition) super.withDeclaredClass(clazz);
	}

	@NonNull
	@Override
	public MethodCondition withAnnotation(@NonNull Class<? extends Annotation> annotation) {
		return (MethodCondition) super.withAnnotation(annotation);
	}

	/**
	 * Add condition by return type
	 *
	 * @param clazz Method return type
	 *
	 */
	@NonNull
	public MethodCondition withReturnType(@NonNull Class<?> clazz) {
		this.condition = this.condition.and((method) -> method.getReturnType() == clazz);
		return this;
	}

	/**
	 * Add condition by parameters count
	 *
	 * @param parametersCount Parameters count
	 *
	 */
	@NonNull
	public MethodCondition withParametersCount(int parametersCount) {
		this.condition = this.condition.and((method) -> method.getParameterCount() == parametersCount);
		return this;
	}

	/**
	 * Add condition by current parameter type
	 *
	 * @param index Parameter index
	 * @param type  Parameter type
	 *
	 */
	@NonNull
	public MethodCondition withParameter(int index, @NonNull Class<?> type) {
		this.condition = this.condition.and((method) -> method.getParameterTypes()[index] == type);
		return this;
	}

	/**
	 * Add condition by parameter types
	 *
	 * @param types Parameter types
	 *
	 */
	@NonNull
	public MethodCondition withParameters(@NonNull Class<?>... types) {
		this.condition = this.condition.and((method) -> Arrays.equals(types, method.getParameterTypes()));
		return this;
	}

}
