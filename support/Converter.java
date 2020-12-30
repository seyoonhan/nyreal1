package com.han.startup.support;

import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

public interface Converter {

	public Property resolveProperty(Type type,
                                    ModelConverterContext context,
                                    Annotation[] annotations,
                                    Iterator<Converter> chain);

	public Model resolve(Type type, ModelConverterContext context, Iterator<Converter> chain);
}