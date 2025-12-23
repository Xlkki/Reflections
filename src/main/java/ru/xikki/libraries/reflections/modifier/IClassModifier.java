package ru.xikki.libraries.reflections.modifier;

import lombok.NonNull;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Java class modificator.<br>
 * For class modification used BCEL API
 * <br><br>
 * <b>Modificable class shouldn't be loaded at runtime</b>
 *
 * @see IClassScanner implementation of IClassModifier$Holder in which you can register modifiers and modify classes
 * @see IClassScanner
 */
public interface IClassModifier {

	/**
	 * Check if class should be modified (by name)
	 *
	 * @param holder    Current class modifier holder
	 * @param className Class name to check
	 * @return True if class should be modified, otherwise - false
	 *
	 */
	boolean shouldModify(@NonNull IClassModifier.Holder holder, @NonNull String className);

	/**
	 * Check if class should be modified
	 *
	 * @param holder    Current class modifier holder
	 * @param javaClass Class to check
	 * @return True if class should be modified, otherwise - false
	 *
	 */
	boolean shouldModify(@NonNull IClassModifier.Holder holder, @NonNull JavaClass javaClass);

	/**
	 * Modify class
	 *
	 * @param holder         Current class modifier holder
	 * @param classGenerator Modifiable class generator
	 *
	 */
	void modifyClass(@NonNull IClassModifier.Holder holder, @NonNull ClassGen classGenerator);

	interface Holder {

		/**
		 * Get registered class modifiers
		 *
		 * @return List with registered class modifiers
		 *
		 */
		@NonNull
		List<IClassModifier> getModifiers();

		/**
		 * Register class modifier
		 *
		 * @param modifier Class modifier to register
		 *
		 */
		@NonNull
		Holder registerModifier(@NonNull IClassModifier modifier);

		/**
		 * Register multiple class modifiers
		 *
		 * @param modifiers Class modifiers to register
		 *
		 */
		@NonNull
		default Holder registerModifiers(@NonNull IClassModifier... modifiers) {
			Arrays.stream(modifiers).forEach(this::registerModifier);
			return this;
		}

		/**
		 * Register multiple class modifiers
		 *
		 * @param modifiers Class modifiers to register
		 *
		 */
		@NonNull
		default Holder registerModifiers(@NonNull Collection<? extends IClassModifier> modifiers) {
			return this.registerModifiers(modifiers.toArray(IClassModifier[]::new));
		}

		/**
		 * Unregister class modifier
		 *
		 * @param modifier Class modifier to unregister
		 *
		 */
		@NonNull
		Holder unregisterModifier(@NonNull IClassModifier modifier);

		/**
		 * Unregister multiple class modifiers
		 *
		 * @param modifiers Class modifiers to unregister
		 *
		 */
		@NonNull
		default Holder unregisterModifiers(@NonNull IClassModifier... modifiers) {
			Arrays.stream(modifiers).forEach(this::unregisterModifier);
			return this;
		}

		/**
		 * Unregister multiple class modifiers
		 *
		 * @param modifiers Class modifiers to unregister
		 *
		 */
		@NonNull
		default Holder unregisterModifiers(@NonNull Collection<? extends IClassModifier> modifiers) {
			return this.unregisterModifiers(modifiers.toArray(IClassModifier[]::new));
		}

		/**
		 * Unregister all registered class modifiers
		 *
		 */
		@NonNull
		default Holder unregisterAllModifiers() {
			return this.unregisterModifiers(this.getModifiers());
		}

		/**
		 * Run all registered modifiers. <br>
		 *
		 * @see Holder#dump(ClassLoader) Dump modifiable classes
		 * */
		@NonNull
		Holder modifyClasses();

		/**
		 * Dump all changed classes into class loader.
		 *
		 * @param loader Class loader
		 * @see Holder#modifyClasses() Run all registered modifiers.<br>Call it before calling that method
		 * */
		@NonNull
		Holder dump(@NonNull ClassLoader loader);

	}

}
