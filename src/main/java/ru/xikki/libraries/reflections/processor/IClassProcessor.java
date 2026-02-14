package ru.xikki.libraries.reflections.processor;

import lombok.NonNull;
import org.apache.bcel.classfile.JavaClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface IClassProcessor {

	/**
	 * Check if specified class should be processed by name
	 *
	 * @param holder    Class processor holder in which processed class founded
	 * @param className Class name
	 * @return True if specified class should be processed, otherwise - false
	 */
	boolean shouldProcess(@NonNull IClassProcessor.Holder holder, @NonNull String className);

	/**
	 * Check if specified class should be processed
	 *
	 * @param holder    Class processor holder in which processed class founded
	 * @param javaClass Class to process
	 * @return True if specified class should be processed, otherwise - false
	 */
	boolean shouldProcess(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass);

	/**
	 * Check if specified class should be loaded
	 *
	 * @param holder    Class processor holder in which processed class founded
	 * @param javaClass Class to load
	 * @return True if specified class should be loaded, otherwise - false
	 */
	boolean shouldLoadClass(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass);

	/**
	 * Check if instance of specified class should be created
	 *
	 * @param holder    Class processor holder in which processed class founded
	 * @param javaClass Class to create instance
	 * @return True if instance of specified class should be created, otherwise - false
	 */
	boolean shouldCreateInstance(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass);

	/**
	 * Process class instance
	 *
	 * @param holder   Class processor holder in which processed class founded
	 * @param instance Processed class instance
	 */
	void processInstance(@NonNull IClassProcessor.Holder holder, @NonNull Object instance);

	/**
	 * Process loaded class
	 *
	 * @param holder Class processor holder in which processed class founded
	 * @param clazz  Processed class
	 */
	void processClass(@NonNull IClassProcessor.Holder holder, @NonNull Class<?> clazz);

	/**
	 * Process class
	 *
	 * @param holder    Class processor holder in which processed class founded
	 * @param javaClass Processed class
	 */
	void processClass(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass);

	interface Holder {

		@NonNull
		List<IClassProcessor> getProcessors();

		@NonNull
		IClassProcessor.Holder registerProcessor(@NonNull IClassProcessor processor);

		@NonNull
		default IClassProcessor.Holder registerProcessors(@NonNull IClassProcessor... processors) {
			Arrays.stream(processors).forEach(this::registerProcessor);
			return this;
		}

		@NonNull
		default IClassProcessor.Holder registerProcessors(@NonNull Collection<? extends IClassProcessor> processors) {
			return this.registerProcessors(processors.toArray(IClassProcessor[]::new));
		}

		@NonNull
		IClassProcessor.Holder unregisterProcessor(@NonNull IClassProcessor processor);

		@NonNull
		default IClassProcessor.Holder unregisterProcessors(@NonNull IClassProcessor... processors) {
			Arrays.stream(processors).forEach(this::unregisterProcessor);
			return this;
		}

		@NonNull
		default IClassProcessor.Holder unregisterProcessors(@NonNull Collection<? extends IClassProcessor> processors) {
			return this.unregisterProcessors(processors.toArray(IClassProcessor[]::new));
		}

		@NonNull
		IClassProcessor.Holder unregisterAllProcessors();

	}

}
