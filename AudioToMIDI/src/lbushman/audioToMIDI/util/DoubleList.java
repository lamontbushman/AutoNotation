package lbushman.audioToMIDI.util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

// Copied and modified from java.util.Arrays.ArrayList
// Be careful with any code that might call toArray().
// It will modify the original array and not a copy.
/**
 * @serial include
 */
public class DoubleList extends AbstractList<Double>
    implements RandomAccess, java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	private final double[] a;

    public DoubleList(double[] array) {
        a = Objects.requireNonNull(array);
    }

    @Override
    public int size() {
        return a.length;
    }

    /**
     * 
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must
     * allocate a new array even if this list is backed by an array).
     * The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this list in proper
     *         sequence
     * @see Arrays#asList(Object[])
     */

    @Override
    public Double[] toArray() {
    	Double[] array = new Double[a.length];
    	Util.logIfFails(false,"to array was called. I hope that this isn't called in my code. Because it would be inefficient");
    	for(int i = 0; i < a.length; i++) {
    		array[i] = a[i];
    	}
    	return array;
//        return a.clone();
    }
    
    public double[] backingArray() {
    	return a;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
    	Util.verify(false, "This must never be called, unless I implement it.");
    	return null;
/*    	
        int size = size();
        if (a.length < size)
            return Arrays.copyOf(this.a, size,
                                 (Class<? extends T[]>) a.getClass());
        System.arraycopy(this.a, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
 */
    }

    @Override
    public Double get(int index) {
        return a[index];
    }

    @Override
    public Double set(int index, Double element) {
    	Double oldValue = a[index];
        a[index] = element;
        return oldValue;
    }

    @Override
    public int indexOf(Object o) {
        //E[] a = this.a;
    	double[] a = this.a;
        if (o == null) {
        	return -1;
/*            for (int i = 0; i < a.length; i++)
                if (a[i] == null)
                    return i;
*/
        } else {
            for (int i = 0; i < a.length; i++)
                if (o.equals(a[i]))
                    return i;
        }
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public Spliterator<Double> spliterator() {
        return Spliterators.spliterator(a, Spliterator.ORDERED);
    }

    @Override
    public void forEach(Consumer<? super Double> action) {
        Objects.requireNonNull(action);
        for (Double e : a) {
            action.accept(e);
        }
    }

    @Override
    public void replaceAll(UnaryOperator<Double> operator) {
        Objects.requireNonNull(operator);
        //E[] a = this.a;
        double[] a = this.a;
        for (int i = 0; i < a.length; i++) {
            a[i] = operator.apply(a[i]); // automatically casts to Double
        }
    }

    @Override
    public void sort(Comparator<? super Double> c) {
    	Arrays.sort(toArray(), c);
    	//Arrays.sort(a, c);
    }
/*    
    // Copied and modified from java.util.Arrays
    *//**
     * Returns a fixed-size list backed by the specified array.  (Changes to
     * the returned list "write through" to the array.)  This method acts
     * as bridge between array-based and collection-based APIs, in
     * combination with {@link Collection#toArray}.  The returned list is
     * serializable and implements {@link RandomAccess}.
     *
     * <p>This method also provides a convenient way to create a fixed-size
     * list initialized to contain several elements:
     * <pre>
     *     List&lt;String&gt; stooges = Arrays.asList("Larry", "Moe", "Curly");
     * </pre>
     *
     * @param <T> the class of the objects in the array
     * @param a the array by which the list will be backed
     * @return a list view of the specified array
     *//*
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> asList(T... a) {
        return new DoubleList<>(a);
    }*/
}
