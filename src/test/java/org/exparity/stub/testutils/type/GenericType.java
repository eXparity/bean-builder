/**
 * 
 */
package org.exparity.stub.testutils.type;

/**
 * @author Stewart Bissett
 */
public class GenericType<T> {

	private T value;

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

}
