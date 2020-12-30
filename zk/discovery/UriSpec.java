package com.han.startup.zk.discovery;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
@NoArgsConstructor
public class UriSpec implements Iterable<UriSpec.Part> {
	public static final String FIELD_SCHEME = "scheme";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_ID = "externalId";
	public static final String FIELD_ADDRESS = "address";
	public static final String FIELD_PORT = "port";
	public static final String FIELD_SSL_PORT = "ssl-port";
	public static final String FIELD_REGISTRATION_TIME_UTC = "registration-time-utc";
	public static final String FIELD_SERVICE_TYPE = "service-type";
	public static final String FIELD_OPEN_BRACE = "[";
	public static final String FIELD_CLOSE_BRACE = "]";
	private final List<Part> parts = Lists.newArrayList();

	public UriSpec(String rawSpec) {
		boolean isInsideVariable = false;
		StringTokenizer tokenizer = new StringTokenizer(rawSpec, "{}", true);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("{")) {
				Preconditions.checkState(!isInsideVariable, "{ is not allowed inside of a variable specification");
				isInsideVariable = true;
			} else if (token.equals("}")) {
				Preconditions.checkState(isInsideVariable, "} must be preceded by {");
				isInsideVariable = false;
			} else {
				if (isInsideVariable) {
					token = token.trim();
				}
				add(new Part(token, isInsideVariable));
			}
		}

		Preconditions.checkState(!isInsideVariable, "Final variable not closed - expected }");
	}

	public void add(Part part) {
		parts.add(part);
	}

	public String build() {
		return build(null, Maps.newHashMap());
	}

	public String build(ServiceInstance<?> serviceInstance, Map<String, Object> variables) {
		Map<String, Object> localVariables = Maps.newHashMap();
		localVariables.put(FIELD_OPEN_BRACE, "{");
		localVariables.put(FIELD_CLOSE_BRACE, "}");
		localVariables.put(FIELD_SCHEME, "http");

		if (serviceInstance != null) {
			localVariables.put(FIELD_NAME, nullCheck(serviceInstance.getName()));
			localVariables.put(FIELD_ID, nullCheck(serviceInstance.getId()));
			localVariables.put(FIELD_ADDRESS, nullCheck(serviceInstance.getAddress()));
			localVariables.put(FIELD_PORT, nullCheck(serviceInstance.getPort()));
			localVariables.put(FIELD_SSL_PORT, nullCheck(serviceInstance.getSslPort()));
			localVariables.put(FIELD_REGISTRATION_TIME_UTC, nullCheck(serviceInstance.getRegistrationTimeUTC()));
			localVariables.put(FIELD_SERVICE_TYPE, (serviceInstance.getServiceType() != null) ? serviceInstance.getServiceType().name().toLowerCase() : "");
			if (serviceInstance.getSslPort() != null) {
				localVariables.put(FIELD_SCHEME, "https");
			}
		}

		localVariables.putAll(variables);

		StringBuilder str = new StringBuilder();
		for (Part p : parts) {
			if (p.isVariable()) {
				Object value = localVariables.get(p.getValue());
				if (value == null) {
					log.debug("Variable not found: " + p.getValue());
				} else {
					str.append(value);
				}
			} else {
				str.append(p.getValue());
			}
		}

		return str.toString();
	}

	private Object nullCheck(Object o) {
		return (o != null) ? o : "";
	}

	public String build(ServiceInstance<?> serviceInstance) {
		return build(serviceInstance, Maps.<String, Object>newHashMap());
	}

	public String build(Map<String, Object> variables) {
		return build(null, variables);
	}

	@NotNull
	@Override
	public Iterator<Part> iterator() {
		return Iterators.unmodifiableIterator(parts.iterator());
	}

	public List<Part> getParts() {
		return ImmutableList.copyOf(parts);
	}

	public void remove(Part part) {
		parts.remove(part);
	}

	public static class Part {
		private final String value;
		private final boolean variable;

		public Part(String value, boolean isVariable) {
			this.value = value;
			this.variable = isVariable;
		}

		public Part() {
			value = "";
			variable = false;
		}

		public String getValue() {
			return value;
		}

		public boolean isVariable() {
			return variable;
		}

		@Override
		public int hashCode() {
			int result = value.hashCode();
			result = 31 * result + (variable ? 1 : 0);
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			Part part = (Part) o;

			if (variable != part.variable) {
				return false;
			}
			if (!value.equals(part.value)) {
				return false;
			}

			return true;
		}


	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		UriSpec spec = (UriSpec) o;

		if (!parts.equals(spec.parts)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return parts.hashCode();
	}


}
