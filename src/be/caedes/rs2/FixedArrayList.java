package be.caedes.rs2;

import java.util.Objects;

public class FixedArrayList<E> {

    private final E[] array;

    public FixedArrayList(E[] array) {
        this.array = array;
    }

    public E set(int index, E e) {
        Objects.checkIndex(index, array.length);
        E oldObject = array[index];
        array[index] = null;
        return oldObject;
    }

    public int add(E e) {
        int index = indexOf(null);
        if (index != -1) array[index] = e;
        return index;
    }

    public int indexOf(E e) {
        if (e == null) {
            for (int i = 0; i < array.length; i++) if (array[i] == null) return i;
        } else {
            for (int i = 0; i < array.length; i++) if (array[i].equals(e)) return i;
        }
        return -1;
    }

    public E get(int index) {
        Objects.checkIndex(index, array.length);
        return array[index];
    }

    public E remove(int index) {
        Objects.checkIndex(index, array.length);
        E removedObject = array[index];
        array[index] = null;
        return removedObject;
    }

    public boolean contains(E e) {
        return indexOf(e) >= 0;
    }

}
