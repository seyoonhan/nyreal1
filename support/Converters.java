package com.han.startup.support;


import com.fasterxml.jackson.databind.type.TypeFactory;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class Converters {
	private static final Converters SINGLETON = new Converters();

	static {
		SINGLETON.skippedPackages.add("java.lang");

		ServiceLoader<DefaultModelConverter> loader = ServiceLoader.load(DefaultModelConverter.class);
		Iterator<DefaultModelConverter> itr = loader.iterator();
		while (itr.hasNext()) {
			DefaultModelConverter ext = itr.next();
			if (ext == null) {
				log.error("failed to load extension ");
			} else {
				SINGLETON.addConverter(ext);
				log.debug("adding Converter: " + ext);
			}
		}
	}

	private final List<DefaultModelConverter> converters;
	private final Set<String> skippedPackages = new HashSet<String>();
	private final Set<String> skippedClasses = new HashSet<String>();

	public Converters() {
		converters = new CopyOnWriteArrayList<DefaultModelConverter>();
	}

	public static Converters getInstance() {
		return SINGLETON;
	}

	public void addConverter(DefaultModelConverter converter) {
		converters.add(0, converter);
	}

	public void removeConverter(DefaultModelConverter converter) {
		converters.remove(converter);
	}

	public void addPackageToSkip(String pkg) {
		this.skippedPackages.add(pkg);
	}

	public void addClassToSkip(String cls) {
		log.warn("skipping class " + cls);
		this.skippedClasses.add(cls);
	}

	public Property readAsProperty(Type type) {
		DefaultModelConverter context = new DefaultModelConverter(
				converters);
		return context.resolveProperty(type, null);
	}

	public Map<String, Model> read(Type type) {
		Map<String, Model> modelMap = new HashMap<String, Model>();
		if (shouldProcess(type)) {
			DefaultModelConverter context = new DefaultModelConverter(
					converters);
			Model resolve = context.resolve(type);
			for (Entry<String, Model> entry : context.getDefinedModels()
					.entrySet()) {
				if (entry.getValue().equals(resolve)) {
					modelMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return modelMap;
	}

	private boolean shouldProcess(Type type) {
		final Class<?> cls = TypeFactory.defaultInstance().constructType(type).getRawClass();
		if (cls.isPrimitive()) {
			return false;
		}
		String className = cls.getName();
		for (String packageName : skippedPackages) {
			if (className.startsWith(packageName)) {
				return false;
			}
		}
		for (String classToSkip : skippedClasses) {
			if (className.equals(classToSkip)) {
				return false;
			}
		}
		return true;
	}

	public Map<String, Model> readAll(Type type) {
		if (shouldProcess(type)) {
			DefaultModelConverter context = new DefaultModelConverter(
					converters);

			log.debug("Converters readAll from " + type);
			context.resolve(type);
			return context.getDefinedModels();
		}
		return new HashMap<String, Model>();
	}
}